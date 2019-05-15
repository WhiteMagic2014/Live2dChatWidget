package com.magic.mail;

import java.util.List;
import java.util.Properties;

import com.magic.ApiService;

/**
 * 企业qq邮箱监听
 * 
 * @author chenhaoyu
 *
 */
public class ExQQMail extends Mail {

	public ExQQMail(ApiService handler, String userName, String passWord, List<String> folders) {
		super(handler, userName, passWord, folders);
	}

	@Override
	public Properties getProperties() {

		String host = "imap.exmail.qq.com";// QQ企业邮箱的imap服务器
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
		return "腾讯企业邮箱";
	}
}
