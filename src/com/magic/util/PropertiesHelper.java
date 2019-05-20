package com.magic.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * 想做配置文件的 但是没有 让我想想怎么做
 * 
 * @author chenhaoyu
 *
 */
public class PropertiesHelper {
	
	public static final String MAIL_ENABLE = "mail.enable";
	public static final String MAIL_QQ_ACC = "mail.qq.account";
	public static final String MAIL_QQ_KEY = "mail.qq.key";
	public static final String MAIL_QQ_FOL = "mail.qq.folders";
	
	
	public static final String MAIL_QQEX_ACC = "mail.qqex.account";
	public static final String MAIL_QQEX_KEY = "mail.qqex.key";
	public static final String MAIL_QQEX_FOL = "mail.qqex.folders";
	
	
	private Properties properties = new Properties();

	String osName = System.getProperty("os.name");

	String separator = System.getProperty("file.separator");

	String userPath = System.getProperty("user.home");

	String folderPath = "";
	String properFilePath = "";

	private static PropertiesHelper instance = new PropertiesHelper();

	public static PropertiesHelper getInstance() {
		return instance;
	}

	private PropertiesHelper() {

		folderPath = userPath + separator + "Live2dChatPlugin";
		properFilePath = userPath + separator + "Live2dChatPlugin" + separator + "setting.properties";

		File folder = new File(folderPath);
		File properFile = new File(properFilePath);

		if (folder.exists()) {
			if (folder.isDirectory()) {
				if (properFile.exists()) {
					System.out.println("找到配置文件");

					// 读配置文件
					try {
						properties.load(new BufferedReader(new FileReader(properFile)));
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					createProperties();
				}
			} else {
				System.out.println("the same name file exists, can not create dir");
			}
		} else {
			System.out.println("创建文件夹");
			folder.mkdir();

			createProperties();
		}

	}
	
	
	/**
	 * 初始化配置文件
	 */
	public void createProperties(){
		System.out.println("创建配置文件");
		properties.setProperty(MAIL_ENABLE, "F");
		properties.setProperty(MAIL_QQ_ACC, "");
		properties.setProperty(MAIL_QQ_KEY, "");
		properties.setProperty(MAIL_QQ_FOL, "");
		
		try (FileOutputStream fous = new FileOutputStream(new File(properFilePath))) {
			properties.store(fous, null);
			fous.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

	/*
	 * 获得配置文件内容
	 */
	public String getValue(String key) {
		return properties.getProperty(key);
	}

	/**
	 * 获得所有配置内容
	 * 
	 * @return
	 */
	public String getAllKeyValues() {
		StringBuilder sb = new StringBuilder();

		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			sb.append(entry.getKey().toString()).append("=").append(entry.getValue().toString()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * 写入配置文件
	 * 
	 * @param map
	 */
	public void setValue(Map<String, String> map) {

		for (Entry<String, String> entry : map.entrySet()) {
			properties.setProperty(entry.getKey(), entry.getValue());
		}

		try (FileOutputStream fous = new FileOutputStream(new File(properFilePath))) {
			properties.store(fous, null);
			fous.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
