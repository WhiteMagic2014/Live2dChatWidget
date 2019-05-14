package com.magic.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import com.magic.ApiService;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IdleManager;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * qq 邮箱的监听
 * 
 * @author chenhaoyu
 *
 */
public class QQMail implements MailInterface {

	private ApiService handler;
	private ExecutorService es;

	private String userName;
	private String passWord;

	private Session session;
	private Store store;

	private IdleManager idleManager;

	private List<IMAPFolder> folderObjects = new ArrayList<IMAPFolder>();

	public QQMail(ApiService handler, ExecutorService es, String userName, String passWord, List<String> folders) {
		this.handler = handler;
		this.es = es;
		this.userName = userName;
		this.passWord = passWord;

		initStore();
		createFoldersListen(folders);
		heartWork();
	}

	/**
	 * 初始化store
	 */
	public void initStore() {
		String protocol = "imap";// 使用imap协议
		boolean isSSL = true;// 使用SSL加密
		String host = "imap.qq.com";// QQ邮箱的pop3服务器
		int port = 993;// 端口

		/*
		 * Properties是一个属性对象，用来创建Session对象
		 */
		Properties props = new Properties();
		props.put("mail.imap.ssl.enable", isSSL);
		props.put("mail.imap.host", host);
		props.put("mail.imap.port", port);
		props.put("mail.transport.protocol", "imap");
		props.put("mail.imap.usesocketchannels", "true");

		/*
		 * Session类定义了一个基本的邮件对话。
		 */
		session = Session.getDefaultInstance(props);

		try {
			store = session.getStore(protocol);
			store.connect(userName, passWord);

			idleManager = new IdleManager(session, es);

			// 应该增加idleManager 的连接监控 要有"心跳"

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void createFoldersListen(List<String> folders) {

		for (String folderName : folders) {
			try {
				IMAPFolder temp = (IMAPFolder) store.getFolder(folderName);
				temp.open(Folder.READ_ONLY);// 在这一步，收件箱所有邮件将被下载到本地

				System.out.println(folderName);
				// Message message = folder.getMessage(size);//取得最新的那个邮件
				System.out.println("Size:" + temp.getMessageCount());
				System.out.println("unread:" + temp.getUnreadMessageCount());
				System.out.println("new:" + temp.getNewMessageCount());
				System.out.println();

				temp.addMessageCountListener(new MessageCountAdapter() {

					@Override
					public void messagesAdded(MessageCountEvent e) {
						super.messagesAdded(e);

						IMAPFolder folder = (IMAPFolder) e.getSource();
						Message[] msgs = e.getMessages();
						for (int i = 0; i < msgs.length; i++) {
							try {
								handler.setModelText("有新邮件！标题：" + msgs[i].getSubject(), 0);
								System.out.println("add 邮件主题" + msgs[i].getSubject());
							} catch (MessagingException e1) {
								e1.printStackTrace();
							}
						}
						try {
							idleManager.watch(folder);
						} catch (MessagingException e1) {
							e1.printStackTrace();
						}

					}

				});
				idleManager.watch(temp);
				folderObjects.add(temp);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	
	/**
	 * 待定的心跳检测
	 */
	public void heartWork() {
		es.execute(new Runnable() {
			@Override
			public void run() {

				while (true) {

					try {
						Thread.sleep(9 * 60 * 1000);// 9分钟一次与邮件服务器的心跳 （gmail
													// 超时时间比较短）

						for (IMAPFolder folder : folderObjects) {
							folder.doCommand(new IMAPFolder.ProtocolCommand() {
								@Override
								public Object doCommand(IMAPProtocol arg0) throws ProtocolException {
									arg0.simpleCommand("NOOP", null);
									return null;
								}
							});
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		});
	}

	@Override
	public void close() {
		try {
			for (Folder folder : folderObjects) {
				if (folder != null) {
					folder.close(false);
				}
			}
			if (store != null) {
				store.close();
			}
			if (idleManager != null && idleManager.isRunning()) {
				idleManager.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
