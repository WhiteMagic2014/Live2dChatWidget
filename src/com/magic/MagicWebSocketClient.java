package com.magic;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
	ScheduledFuture<?> future = null;

	public MagicWebSocketClient(String serverUri, ApiService handler) throws URISyntaxException {
		super(new URI(serverUri));
		this.handle = handler;
	}

	@Override
	public void onClose(int arg0, String arg1, boolean arg2) {
		// 标记ws不可用
		handle.setWSFlag(false);
		// 是否由服务器关闭连接(外部断开视为意外断开) 如果是的话 就重连
		if (arg2) {
			magicReconnect();
		} else {
			future.cancel(true);
			System.out.println("WebSocket onClose");
		}
	}

	@Override
	public void onError(Exception e) {
		System.out.println("WebSocket onError");
		handle.setWSFlag(false);
		e.printStackTrace();
	}

	@Override
	public void onMessage(String result) {
		System.out.println("WebSocket onMessage " + result);
		JSONObject resultObj = JSONObject.parseObject(result);
		handle.processCallback(resultObj);
	}

	@Override
	public void onOpen(ServerHandshake shake) {
		System.out.println("WebSocket onOpen...");
		for (Iterator<String> it = shake.iterateHttpFields(); it.hasNext();) {
			String key = it.next();
			System.out.println(key + ":" + shake.getFieldValue(key));
		}
		handle.setWSFlag(true);
		// (重新)开启心跳任务
		startHeart();
	}

	/**
	 * 开始心跳任务
	 */
	public void startHeart() {
		future = SingleThreadPool.getInstance().scheduledThreadPool().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					handle.setModelText("一颗心扑通扑通的狂跳~", 0);// 默认让第一个接受心跳
					System.out.println(System.currentTimeMillis() + "————WebSocket Heart");
				} catch (Exception e) {
					System.out.println("心跳出错");
					e.printStackTrace();
				}
			}
		}, 0, 20, TimeUnit.MINUTES);// 20分钟一次心跳包
	}

	/**
	 * 断线重连重连
	 */
	public void magicReconnect() {
		// 重连前 先取消之前那个心跳任务
		future.cancel(true);
		// 重连
		SingleThreadPool.getInstance().threadPool().submit(new Runnable() {
			@Override
			public void run() {
				try {
					boolean result = reconnectBlocking();
					if (result) {
						System.out.println("重连成功..");
					} else {
						System.out.println("重连失败..");
					}
				} catch (InterruptedException e) {
					System.out.println("重连失败..");
					e.printStackTrace();
				}
			}
		});

	}

}
