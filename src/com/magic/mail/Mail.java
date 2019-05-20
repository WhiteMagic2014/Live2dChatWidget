package com.magic.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import com.magic.ApiService;
import com.magic.SingleThreadPool;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IdleManager;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * 所有mail实例的父级抽象类 统一使用imap 协议
 * 
 * @author chenhaoyu
 *
 */
public abstract class Mail implements MailInterface {

	// handler 和 线程池 所有Mail子类公用 构造方法中直接传递
	protected ApiService handler;
	protected ExecutorService es;

	// 账号密码
	protected String userName;
	protected String passWord;

	// 以下 session 和store 等 独立使用 后期自己初始化
	protected Session session;
	protected Store store;
	protected IdleManager idleManager;
	protected List<IMAPFolder> folderObjects;

	public Mail(ApiService handler, String userName, String passWord, List<String> folders) {
		this.handler = handler;
		this.es = SingleThreadPool.getInstance().threadPool();

		this.userName = userName;
		this.passWord = passWord;

		try {
			initStore();
			createFoldersListen(folders);
			heartWork();
		} catch (Exception e) {
			e.printStackTrace();
			handler.setModelText("监听" + getMailName() + "失败", 0);
		}
	}

	// 初始化session store idleManager folderObjects
	public void initStore() throws Exception {
		String protocol = "imap";// 使用的协议

		// Properties是一个属性对象，用来创建Session对象
		Properties props = getProperties();

		// Session类定义了一个基本的邮件对话。
		// getDefaultInstance得到的始终是该方法初次创建的缺省的对象，而getInstance得到的始终是新的对象
		// session = Session.getDefaultInstance(props);
		session = Session.getInstance(props);

		store = session.getStore(protocol);
		store.connect(userName, passWord);
		idleManager = new IdleManager(session, es);
		folderObjects = new ArrayList<IMAPFolder>();

	};

	public abstract Properties getProperties();

	public abstract String getMailName();

	// 创建监听
	public void createFoldersListen(List<String> folders) throws Exception {

		for (String folderName : folders) {
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
							handler.setModelTextWithHoldTime(getMailName() + "有新邮件！标题：" + msgs[i].getSubject(), 0, 10);
							System.out.println("新邮件主题" + msgs[i].getSubject());
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
		}
	}

	/**
	 * 待定的心跳检测
	 */
	public void heartWork() {
		// es.execute(new Runnable() {
		// @Override
		// public void run() {
		//
		// while (true) {
		//
		// try {
		// Thread.sleep(9 * 60 * 1000);// 9分钟一次与邮件服务器的心跳 （gmail 超时时间比较短）
		//
		// for (IMAPFolder folder : folderObjects) {
		// folder.doCommand(new IMAPFolder.ProtocolCommand() {
		// @Override
		// public Object doCommand(IMAPProtocol arg0) throws ProtocolException {
		// arg0.simpleCommand("NOOP", null);
		// return null;
		// }
		// });
		// }
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// }
		//
		// }
		// });

		SingleThreadPool.getInstance().scheduledThreadPool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getName() + "————" + getMailName());
//				try {
//					for (IMAPFolder imapFolder : folderObjects) {
//						imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
//							@Override
//							public Object doCommand(IMAPProtocol arg0) throws ProtocolException {
//								arg0.simpleCommand("NOOP", null);
//								return null;
//							}
//						});
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		}, 5, 5, TimeUnit.MINUTES);

	}

	// 关闭 监听 文件夹 store
	@Override
	public void close() {
		try {
			if (idleManager != null && idleManager.isRunning()) {
				idleManager.stop();
			}
			for (Folder folder : folderObjects) {
				if (folder != null) {
					folder.close(false);
				}
			}
			if (store != null) {
				store.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
