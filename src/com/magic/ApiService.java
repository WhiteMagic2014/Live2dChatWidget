package com.magic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ApiService {

	MagicWebSocketClient wsClient = null;

	boolean WSFlag = false;

	public ApiService(String port) {
		try {
			wsClient = new MagicWebSocketClient("ws://127.0.0.1:" + port + "/api", this);
			wsClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 改变状态
	 */
	public void setWSFlag(boolean state) {
		this.WSFlag = state;
	}

	/**
	 * 获得ws是否连接成功
	 * 
	 * @return
	 */
	public boolean getWsFlag() {
		return WSFlag;
	}

	/**
	 * 关闭ws
	 */
	public void closeClient() {
		if (wsClient != null) {
			wsClient.close();
		}
	}

	/**
	 * 让指定model说话
	 * 
	 * @param str
	 * @param modelId
	 */
	public void setModelText(String str, int modelId) {

		JSONObject textjson = new JSONObject();

		textjson.put("msg", 11000);
		textjson.put("msgId", 1);
		JSONObject data = new JSONObject();
		data.put("id", modelId);
		data.put("text", str);
		data.put("duration", 5000);
		textjson.put("data", data);

		wsClient.send(textjson.toJSONString());
	}
	
	/**
	 * 模型延时说话 /主要用于菜单选项之后 ，点击菜单后气泡框立刻消失 导致马上恢复的语言无法显示，所以延时0.1秒发送
	 * 
	 * @param str
	 * @param modelId
	 */
	public void setModelTextDelay(String str, int modelId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
					setModelText(str, modelId);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 让指定model打开菜单
	 * 
	 * @param modelId
	 */
	public void openMenu(int modelId) {
		JSONObject textjson = new JSONObject();

		textjson.put("msg", 11000);
		textjson.put("msgId", 1);
		JSONObject data = new JSONObject();
		data.put("id", modelId);
		data.put("text", "需要我帮忙吗？");
		data.put("duration", 5000);

		JSONArray menu = new JSONArray();
		menu.add("打开maven仓库");
		menu.add("打开工作环境");
		menu.add("随机双色球");

		data.put("choices", menu);
		textjson.put("data", data);
		wsClient.send(textjson.toJSONString());
	}



	/**
	 * 
	 * @param resultObj
	 */
	public void processCallback(JSONObject resultObj) {

		System.out.println(resultObj);

		// 选项回调
		if (resultObj.getIntValue("msg") == 11000) {
			switch (resultObj.getIntValue("data")) {

			case 0:
				shShell("open /Users/chenhaoyu/.m2/repository/");
				break;

			case 1:
				shShell("open /Users/chenhaoyu/Documents/workspace-sts-3.9.5.RELEASE/");
				break;

			case 2:
				String result = createLottery();
				setModelTextDelay(result, 0);
				break;
				
			default:
				setModelTextDelay("=￣ω￣=", 0);
				break;
			}
		}

	}

	/**
	 * 随机彩票
	 * 
	 * @return
	 */
	public String createLottery() {
		StringBuilder sb = new StringBuilder();

		sb.append("猜不中别怪我，猜中了请联系我赏顿饭钱！！！我开始随便猜啦~");
		sb.append("红色:");
		red().stream().forEach(red -> sb.append(red).append(" "));
		sb.append("蓝色:").append(bule());

		return sb.toString();
	}

	private List<Integer> red() {
		Random random = new Random();
		List<Integer> pool = new ArrayList<>();
		List<Integer> result = new ArrayList<>();
		for (int i = 1; i <= 33; i++) {
			pool.add(i);
		}
		for (int i = 1; i <= 6; i++) {
			int flag = random.nextInt(pool.size());
			int redtemp = pool.get(flag);
			result.add(redtemp);
			pool.remove(flag);
		}
		return result.stream().sorted(Integer::compareTo).collect(Collectors.toList());
	}

	private Integer bule() {
		Random random = new Random();
		return (random.nextInt(16) + 1);
	}

	/**
	 * 解析返回的数据 json unicode等
	 * 
	 * @param str
	 * @return
	 */
	public String analysisStr(String str) {
		str = str.replace("[name]", "Master").replace("[cqname]", "我").replace("NULL", "=￣ω￣=");
		return str;
	}

	/**
	 * 实执行shell
	 * 
	 * @param command
	 *            暂时封装了 进仓库 和工作环境
	 * @return
	 */
	public String shShell(String command) {
		if (command.trim().equals("")) {
			return "你要我做什么呢？";
		}
		if (command.contains("rm") || command.contains("chmod")) {
			return "呀屎啦你 作死？";
		} else if (command.contains("top")) {
			return "不支持动态显示结果";
		}

		System.out.println("shell执行记录:" + command);
		String returnString = "";
		Process pro = null;
		Runtime runTime = Runtime.getRuntime();
		if (runTime == null) {
			System.err.println("Create runtime false!");
		}
		try {
			pro = runTime.exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				returnString = returnString + line + "\n";
			}
			input.close();
			output.close();
			pro.destroy();
		} catch (IOException ex) {
			ex.printStackTrace();
			return "被玩坏了_(:з」∠)_";
		}
		return returnString;
	}

	/**
	 * 调用聊天接口
	 * 
	 * @param str
	 * @return
	 */
	public String chat(String str) {
		StringBuffer buffer = new StringBuffer();
		URL url = null;
		try {
			url = new URL("http://i.itpk.cn/api.php?question=" + str);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		HttpURLConnection httpUrlConn = null;
		try {
			httpUrlConn = (HttpURLConnection) url.openConnection();
			httpUrlConn.setDoOutput(false);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			httpUrlConn.setRequestMethod("GET");
			httpUrlConn.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (InputStream inputStream = httpUrlConn.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {
			String result = null;
			while ((result = bufferedReader.readLine()) != null) {
				buffer.append(result);
			}
		} catch (Exception e) {
			return "被玩坏了_(:з」∠)_";
		} finally {
			httpUrlConn.disconnect();
		}
		return buffer.toString();
	}

}