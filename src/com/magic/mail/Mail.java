package com.magic.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
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
		// 方便调试 加上监听
		store.addConnectionListener(new ConnectionListener() {
			@Override
			public void opened(ConnectionEvent e) {
				// handler.setModelText(getMailName() + "已经连接", 0);
				System.out.println("_____邮箱" + getMailName() + " opened");
			}

			@Override
			public void disconnected(ConnectionEvent e) {
				// handler.setModelText(getMailName() + " disconnected", 0);
				System.out.println("_____邮箱" + getMailName() + " disconnected");
			}

			@Override
			public void closed(ConnectionEvent e) {
				// handler.setModelText(getMailName()+" closed" , 0);
				System.out.println("_____邮箱" + getMailName() + " closed");
			}
		});
		store.connect(userName, passWord);

		idleManager = new IdleManager(session, SingleThreadPool.getInstance().threadPool());
		folderObjects = new ArrayList<IMAPFolder>();
	};

	public abstract Properties getProperties();

	public abstract String getMailName();

	// 文件夹watch
	public void createFoldersListen(List<String> folders) throws Exception {

		for (String folderName : folders) {
			IMAPFolder temp = (IMAPFolder) store.getFolder(folderName);
			// 连接监听
			temp.addConnectionListener(new ConnectionListener() {
				@Override
				public void opened(ConnectionEvent e) {
					System.out.println("文件夹:" + getMailName() + folderName + " opened");
				}

				@Override
				public void disconnected(ConnectionEvent e) {
					System.out.println("文件夹:" + getMailName() + folderName + " disconnected");
				}

				@Override
				public void closed(ConnectionEvent e) {
					System.out.println("文件夹:" + getMailName() + folderName + " closed");
				}
			});

			temp.open(Folder.READ_ONLY);// 在这一步，收件箱所有邮件将被下载到本地

			// Message message = folder.getMessage(size);//取得最新的那个邮件
			System.out.println("Size:" + temp.getMessageCount());
			System.out.println("unread:" + temp.getUnreadMessageCount());
			System.out.println("new:" + temp.getNewMessageCount());
			System.out.println();
			// 收件监听
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
					} catch (MessagingException e2) {
						e2.printStackTrace();
					}

				}

			});

			idleManager.watch(temp);
			folderObjects.add(temp);

			handler.setModelText(getMailName() + " " + folderName + " 已关注", 0);
		}
	}

	/**
	 * 文件夹心跳检测
	 */
	public void heartWork() {

		SingleThreadPool.getInstance().scheduledThreadPool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println(Thread.currentThread().getName() + "————" + getMailName());
					for (IMAPFolder imapFolder : folderObjects) {
						try {
							imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {
								@Override
								public Object doCommand(IMAPProtocol arg0) throws ProtocolException {
									arg0.simpleCommand("NOOP", null);
									return null;
								}
							});
						} catch (FolderClosedException cfe) {
							cfe.printStackTrace();
							if (!imapFolder.isOpen()) {
								imapFolder.open(Folder.READ_ONLY);
							}
						}
						// 一定要重新watch
						idleManager.watch(imapFolder);
					}
				} catch (Exception e) {
					System.out.println("让老子看看你葫芦里装的什么B " + e.getMessage());
					e.printStackTrace();
				}
			}

		}, 9, 9, TimeUnit.MINUTES);

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

	@Override
	public void check() {
		System.out.println();
		System.out.println("####################");
		System.out.println(getMailName() + " 当前状态:");
		System.out.println("store是否连接: " + store.isConnected());
		System.out.println("IdleManager是否运行:" + idleManager.isRunning());
		folderObjects.stream().map(
				folder -> folder.getFullName() + ":Subscribe-" + folder.isSubscribed() + ",open-" + folder.isOpen())
				.forEach(System.out::println);
		System.out.println("####################");
		System.out.println();
	}

}
