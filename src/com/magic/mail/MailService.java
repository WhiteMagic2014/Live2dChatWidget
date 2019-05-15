package com.magic.mail;

import java.util.ArrayList;
import java.util.List;

import com.magic.ApiService;

public class MailService {

	/**
	 * QQ邮箱
	 */
	public static final String QQ = "qq";

	/**
	 * 企业qq邮箱
	 */
	public static final String QQEX = "qqex";

	/**
	 * 网易163
	 */
	public static final String WY163 = "wy163";
	
	/**
	 * sina
	 */
	public static final String SINA = "sina";
	
	
	
	// 负责某一个邮箱的监听实例
	private List<MailInterface> mailHandlers;
	// api代理类
	private ApiService handler = null;

	public MailService(ApiService handler) {
		mailHandlers = new ArrayList<MailInterface>();
		this.handler = handler;
	}

	/**
	 * 
	 * 新建邮箱监听
	 * 
	 * @param type
	 *            邮箱类别
	 * @param userName
	 *            用户名
	 * @param passWord
	 *            密码
	 */
	public void addMailListener(String type, String userName, String passWord, List<String> folders) {

		switch (type) {
		case QQ:
			MailInterface qqMail = new QQMailPro(handler, userName, passWord, folders);
			mailHandlers.add(qqMail);
			break;

		case QQEX:
			MailInterface qqexMail = new ExQQMail(handler, userName, passWord, folders);
			mailHandlers.add(qqexMail);
			break;
			
			
		case WY163:
			MailInterface wy163Mail = new Wy163Mail(handler, userName, passWord, folders);
			mailHandlers.add(wy163Mail);
			break;
			
		case SINA:
			MailInterface sinaMail = new SinaMail(handler, userName, passWord, folders);
			mailHandlers.add(sinaMail);
			break;
			
		default:
			break;
		}

	}

	/**
	 * 关闭 所有mail的 监听 文件夹 store
	 */
	public void close() {
		mailHandlers.forEach(mh -> mh.close());
	}

}
