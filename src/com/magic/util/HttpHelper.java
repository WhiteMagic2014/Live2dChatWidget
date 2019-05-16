package com.magic.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

	public static String errInfo = "被玩坏了_(:з」∠)_";

	public static String sendGet(String req_url) {
		StringBuffer buffer = new StringBuffer();
		URL url = null;
		try {
			url = new URL(req_url);
		} catch (Exception e) {
			return errInfo;
		}
		HttpURLConnection httpUrlConn = null;

		try {
			httpUrlConn = (HttpURLConnection) url.openConnection();
			httpUrlConn.setDoOutput(false);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			httpUrlConn.setRequestMethod("GET");
			httpUrlConn.connect();
		} catch (Exception e) {
			return errInfo;
		}

		try (InputStream inputStream = httpUrlConn.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {

			String result = null;
			while ((result = bufferedReader.readLine()) != null) {
				buffer.append(result);
			}
		} catch (Exception e) {
			return errInfo;
		} finally {
			httpUrlConn.disconnect();
		}
		return buffer.toString();
	}

}
