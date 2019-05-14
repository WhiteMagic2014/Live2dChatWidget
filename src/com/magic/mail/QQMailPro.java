package com.magic.mail;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import com.magic.ApiService;

/**
 * 另一种更快捷的方式实现qqMail
 * 
 * @author chenhaoyu
 *
 */
public class QQMailPro extends Mail {

	public QQMailPro(ApiService handler, ExecutorService es, String userName, String passWord, List<String> folders) {
		super(handler, es, userName, passWord, folders);
	}

	@Override
	public Properties getProperties() {
		String host = "imap.qq.com";// QQ邮箱的imap服务器
		int port = 993;// 端口

		Properties props = new Properties();
		props.put("mail.imap.ssl.enable", "true");
		props.put("mail.imap.host", host);
		props.put("mail.imap.port", port);
		props.put("mail.transport.protocol", "imap");
		props.put("mail.imap.usesocketchannels", "true");

		return props;
	}

}
