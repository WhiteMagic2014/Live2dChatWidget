package com.magic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.magic.mail.MailService;
import com.magic.util.PropertiesHelper;

//启动类
public class Simulator {

	public static void main(String[] args) {

		// 读取配置文件
		PropertiesHelper.getInstance();

		// 先启一般服务
		ApiService apiService = new ApiService("10086");// 预留了接上托盘软件的可能性

		// 根据配置看是否开启邮箱监听
		String enableMail = PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_ENABLE);
		if (StringUtils.isNotBlank(enableMail) && enableMail.equals("T")) {
			// qq邮箱
			if (StringUtils.isNotBlank(PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_ACC))) {
				String qqfold = PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_FOL);
				List<String> fold = Arrays.asList(qqfold.split(",")).stream().filter(s->StringUtils.isNotBlank(s)).map(s -> "其他文件夹/" + s)
						.collect(Collectors.toList());
				fold.add("inbox");
				apiService.addMailListener(MailService.QQ,
						PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_ACC),
						PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_KEY), fold);
			}

//			List<String> defaultFolders = new ArrayList<String>();
//			defaultFolders.add("inbox");
//
//			// 企业qq邮箱
//			apiService.addMailListener(MailService.QQEX, "企业邮箱", "密码", defaultFolders);
//			// 网易163 服务器不支持idle监听 懒得写轮询 先放着吧
//			apiService.addMailListener(MailService.WY163, "xxxx@163.com", "授权码", defaultFolders);
//			// 新浪 服务器不支持idle监听
//			apiService.addMailListener(MailService.SINA, "xxxxx@sina.com", "账号密码", defaultFolders);
		}

		// 开始绘图
		ChatRobotGUI gui = new ChatRobotGUI(apiService);

		// 给窗口浮动开一个独立线程
		SingleThreadPool.getInstance().threadPool().execute(gui);
	}

}
