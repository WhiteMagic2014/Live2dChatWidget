package com.magic.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * 想做配置文件的 但是没有 让我想想怎么做
 * @author chenhaoyu
 *
 */
public class PropertiesHelper {

	private Properties properties;
	public String separator = System.getProperty("file.separator");

	private static PropertiesHelper instance = new PropertiesHelper();

	public static PropertiesHelper getInstance() {
		return instance;
	}

	private PropertiesHelper() {

		properties = new Properties();
		try {
			properties.load(PropertiesHelper.class.getResourceAsStream("setting.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 获得配置文件内容
	 */
	public String getValue(String key) {
		return properties.get(key).toString();
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
			sb.append(entry.getKey().toString()).append("=").append(entry.getValue().toString()).append(".");
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

		String f = PropertiesHelper.class.getResource("setting.properties").getFile();

		try (FileOutputStream fous = new FileOutputStream(f);) {
			properties.store(fous, null);
			fous.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
