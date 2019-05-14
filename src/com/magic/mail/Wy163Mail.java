package com.magic.mail;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import com.magic.ApiService;

/**
 * 网易163邮箱 貌似不能监听有点问题
 * 
 * @author chenhaoyu
 *
 */
public class Wy163Mail extends Mail {

	public Wy163Mail(ApiService handler, ExecutorService es, String userName, String passWord, List<String> folders) {
		super(handler, es, userName, passWord, folders);
	}

	@Override
	public Properties getProperties() {
		String host = "imap.163.com";// QQ企业邮箱的imap服务器
		int port = 993;// 端口

		Properties props = new Properties();
		props.put("mail.imap.ssl.enable", "true");
		props.put("mail.imap.host", host);
		props.put("mail.imap.port", port);
		props.put("mail.transport.protocol", "imap");
		props.put("mail.imap.usesocketchannels", "true");

		return props;
	}
	
	@Override
	public String getMailName() {
		return "网易163邮箱";
	}
}
