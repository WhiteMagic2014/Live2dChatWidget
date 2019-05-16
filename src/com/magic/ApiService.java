package com.magic;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.magic.mail.MailService;
import com.magic.util.HttpHelper;

public class ApiService {

	MagicWebSocketClient wsClient = null;

	MailService mailService = null;

	boolean WSFlag = false;

	public ApiService(String port) {
		try {
			wsClient = new MagicWebSocketClient("ws://127.0.0.1:" + port + "/api", this);
			wsClient.connect();
		} catch (Exception e) {
			System.out.println("ws连接失败");
			e.printStackTrace();
		}

		mailService = new MailService(this);
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
	 * 关闭插件 所有需要关闭的统一在这里关
	 */
	public void totalClose() {
		setModelText("插件关闭 感谢使用", 0);

		System.out.println("停止线程中的任务");// 窗体抖动 ws心跳 延迟发言
		SingleThreadPool.getInstance().close();

		System.out.println("关闭mail相关监听");
		mailService.close();

		System.out.println("关闭websocket连接");
		if (wsClient != null) {
			wsClient.close();
		}
		System.exit(0);
	}

	/**
	 * 做一个代理 把mailService封装在里面
	 * 
	 * @param type
	 * @param userName
	 * @param passWord
	 * @param folders
	 */
	public void addMailListener(String type, String userName, String passWord, List<String> folders) {
		mailService.addMailListener(type, userName, passWord, folders);
	}

	/**
	 * 让指定model说话
	 * 
	 * @param str
	 * @param modelId
	 */
	public void setModelText(String str, int modelId) {
		setModelTextWithHoldTime(str, modelId, 5);
	}

	/**
	 * 让指定model说话 文字持续保留一段时间
	 * 
	 * @param str
	 * @param modelId
	 * @param holdTime
	 *            秒
	 */
	public void setModelTextWithHoldTime(String str, int modelId, int holdTime) {

		JSONObject textjson = new JSONObject();

		textjson.put("msg", 11000);
		textjson.put("msgId", 1);
		JSONObject data = new JSONObject();
		data.put("id", modelId);
		data.put("text", str);
		data.put("duration", holdTime * 1000);
		textjson.put("data", data);

		wsClient.send(textjson.toJSONString());
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
		menu.add("番剧信息");
		menu.add("看看本项目");
		menu.add("关闭插件");

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
				setModelText(result, 0);
				break;

			case 3:
				String bangumi = getBangumi();
				setModelText(bangumi, 0);
				break;

			case 4:
				URI uri = URI.create("https://github.com/WhiteMagic2014/Live2dChatWidget");
				Desktop dp = Desktop.getDesktop();
				if (dp.isSupported(Desktop.Action.BROWSE)) {
					try {
						dp.browse(uri);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					setModelText("不支持呢...手动打开吧 https://github.com/WhiteMagic2014/Live2dChatWidget", 0);
				}

				break;

			case 5:
				this.totalClose();
				break;

			default:
				setModelText("=￣ω￣=", 0);
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
		str = str.replace("[name]", "{$username}").replace("[cqname]", "我").replace("NULL", "=￣ω￣=");
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
		return HttpHelper.sendGet("http://i.itpk.cn/api.php?question=" + str);
	}

	/**
	 * 番剧信息
	 * 
	 * @return
	 */
	public String getBangumi() {
		String origin = HttpHelper.sendGet("https://bangumi.bilibili.com/web_api/timeline_global");

		JSONObject jsonRaw = JSONObject.parseObject(origin);

		if (jsonRaw.getString("message").equals("success")) {

			StringBuilder sb = new StringBuilder();

			JSONArray dateArray = jsonRaw.getJSONArray("result");
			List<JSONObject> dateList = JSONArray.parseArray(dateArray.toJSONString(), JSONObject.class);

			JSONObject todayData = dateList.stream().filter(date -> date.getBoolean("is_today"))
					.collect(Collectors.toList()).get(0);

			String[] dateTemp = todayData.getString("date").split("-");
			String today = dateTemp[0] + "月" + dateTemp[1] + "日";
			String week = todayData.getString("day_of_week");

			sb.append("今天是 " + today + " 星期" + week + ".\n更新的番剧有:\n");

			// System.out.println("更新的番剧有:");
			List<JSONObject> bangumiList = JSONArray.parseArray(todayData.getString("seasons"), JSONObject.class);

			bangumiList.stream().forEach(t -> {
				sb.append(t.getString("pub_time") + " " + t.getString("title") + " " + t.getString("pub_index") + "\n");
			});

			return sb.toString();
		} else {
			return "获取信息失败_(:з」∠)_";
		}

	}

}
