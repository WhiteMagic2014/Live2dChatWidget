package com.magic;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.alibaba.fastjson.JSONObject;

/**
 * 与live2dviewerEX 通讯的client
 * 
 * @author chenhaoyu
 *
 */
public class MagicWebSocketClient extends WebSocketClient {

	ApiService handle = null;
	Timer timer = null;//心跳timer

	public MagicWebSocketClient(String serverUri, ApiService handler) throws URISyntaxException {
		super(new URI(serverUri));
		this.handle = handler;
	}

	@Override
	public void onClose(int arg0, String arg1, boolean arg2) {
		if (arg2) {
			reconnect();
			System.out.println("断线重连..");
		} else {
			// 结束心跳
			if (timer != null) {
				timer.cancel();
			}
			System.out.println("关闭...");
		}
	}

	@Override
	public void onError(Exception e) {
		System.out.println("异常");
		handle.setWSFlag(false);
		e.printStackTrace();
	}

	@Override
	public void onMessage(String result) {
		System.out.println("接收到消息：" + result);
		JSONObject resultObj = JSONObject.parseObject(result);
		handle.processCallback(resultObj);
	}

	@Override
	public void onOpen(ServerHandshake shake) {
		System.out.println("握手...");
		for (Iterator<String> it = shake.iterateHttpFields(); it.hasNext();) {
			String key = it.next();
			System.out.println(key + ":" + shake.getFieldValue(key));
		}
		handle.setWSFlag(true);
		startHeart();
	}

	/**
	 * 开始发送心跳
	 */
	public void startHeart() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handle.setModelText("一颗心扑通扑通的狂跳~", 0);// 默认当第一个接受心跳
			}
		}, 0, 20 * 60 * 1000);// 这个是毫秒 20分钟一次心跳包
	}

}
