package com.magic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.magic.mail.MailService;
import com.magic.util.PropertiesHelper;

//启动类
public class Simulator {

	public static void main(String[] args) {
		PropertiesHelper.getInstance();
		// 先启一般服务
		ApiService apiService = new ApiService("10086");// 预留了接上托盘软件的可能性

		// 启动邮箱
		String enableMail = PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_ENABLE);
		if (StringUtils.isNotBlank(enableMail) && enableMail.equals("T")) {
			// qq邮箱
			if (StringUtils.isNotBlank(PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_ACC))) {
				String qqfold = PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_FOL);
				List<String> fold = Arrays.asList(qqfold.split(",")).stream().filter(s -> StringUtils.isNotBlank(s))
						.map(s -> "其他文件夹/" + s).collect(Collectors.toList());
				fold.add("inbox");
				apiService.addMailListener(MailService.QQ,
						PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_ACC),
						PropertiesHelper.getInstance().getValue(PropertiesHelper.MAIL_QQ_KEY), fold);
			}
		}
		// 开始绘图
		ChatRobotGUI gui = new ChatRobotGUI(apiService);
		// 给窗口浮动开一个独立线程
		SingleThreadPool.getInstance().threadPool().execute(gui);
	}

}
