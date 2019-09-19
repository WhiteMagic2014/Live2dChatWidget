package com.magic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import com.sun.awt.AWTUtilities;

public class ChatRobotGUI implements ActionListener, Runnable {

	public String version = "1.2.0";
	
	ApiService apiService;

	JFrame fatherFrame, switchFrame;
	JPanel lablePanel, inputPanel, linkPanel;
	JTextField inputField;
	JLabel label;
	// 做拖动用的 记录的鼠标原始坐标
	int xOld = 0;
	int yOld = 0;
	// 做框体浮动 记录目前框体的坐标
	int xnow = 0;
	int ynow = 0;

	Color bg = new Color(0x1e969696, true);
	Color inputBorderColor = new Color(0x1eFFFAFA, true);

	Color bg_control = new Color(0x7d969696, true);

	Boolean hideFlag = true;

	public ChatRobotGUI(ApiService apiService) {
		this.apiService = apiService;
		// 控制开关Frame
		switchFrame = new JFrame();
		switchFrame.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (hideFlag) {
					magicHide();
					hideFlag = false;
				} else {
					magicShow();
					hideFlag = true;
				}
			}
		});
		switchFrame.setSize(20, 20);
		switchFrame.setUndecorated(true);// 禁用或启动用此JFrame装饰
		switchFrame.setBackground(bg_control); // 将背景色设置为空
		switchFrame.setAlwaysOnTop(true);// 窗体置顶
		switchFrame.setVisible(true);
		switchFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 本体父窗体Frame
		fatherFrame = new JFrame();
		fatherFrame.setLayout(new BorderLayout());

		// 输出panel
		lablePanel = new JPanel();
		lablePanel.setBackground(bg);
		label = new JLabel();
		label.setSize(fatherFrame.getWidth(), 0);
		label.setForeground(Color.WHITE);
		label.setFont(new Font("黑体", Font.PLAIN, 18));
		label.setBackground(bg);
		label.setVisible(true);
		String notice = "<html>版本:"+version+"<br/>AI由茉莉机器人提供<br/>仅供娱乐学习,严禁商业使用<br/>by Magic2014<br/>感谢Live2DViewerEX作者</html>";
		label.setText(notice);
		lablePanel.add(label);
		fatherFrame.add(lablePanel, BorderLayout.CENTER);

		// 输入panel
		inputPanel = new JPanel();
		inputPanel.setBackground(bg);
		inputField = new JTextField(20);
		inputField.setEditable(true);
		inputField.setEnabled(true);
		inputField.addActionListener(this);
		inputField.setBackground(bg);
		MatteBorder border = new MatteBorder(0, 0, 2, 0, inputBorderColor);
		inputField.setBorder(border);
		inputPanel.add(inputField);
		fatherFrame.add(inputPanel, BorderLayout.SOUTH);

		// 快捷栏panel
		linkPanel = new JPanel();
		linkPanel.setPreferredSize(null);
		linkPanel.setBackground(bg);
		linkPanel.setVisible(true);

		JButton menuBtn = new JButton();
		menuBtn.setForeground(Color.white);
		menuBtn.setText("<html>菜<br>单</html>");
		menuBtn.setBackground(bg);
		menuBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				apiService.openMenu(0);
			}
		});
		menuBtn.setBorderPainted(false);
		menuBtn.setBorder(null);

		linkPanel.add(menuBtn);
		fatherFrame.add(linkPanel, BorderLayout.EAST);

		// 获取开始平移时的点击坐标
		fatherFrame.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				xOld = e.getX();// 记录鼠标按下时的坐标
				yOld = e.getY();
			}

		});

		// 平移时 根据上面获取的坐标 实时计算当前坐标 同时平移 控制和聊天 2个frame
		fatherFrame.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int xOnScreen = e.getXOnScreen();
				int yOnScreen = e.getYOnScreen();
				int xx = xOnScreen - xOld;
				int yy = yOnScreen - yOld;

				fatherFrame.setLocation(xx, yy);// 设置拖拽后，窗口的位置

				xnow = (int) fatherFrame.getLocation().getX();
				ynow = (int) fatherFrame.getLocation().getY();

				switchFrame.setLocation(xnow, ynow);
			}
		});

		fatherFrame.setUndecorated(true);// 禁用或启动用此JFrame装饰
		fatherFrame.setBackground(bg); // 将背景色设置为空

		fatherFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		fatherFrame.setSize(400, 200);
		fatherFrame.setBackground(bg);
		xnow = (int) fatherFrame.getLocation().getX();
		ynow = (int) fatherFrame.getLocation().getY();
		fatherFrame.setVisible(true);

	}

	/**
	 * input 回车监听
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == inputField) {
			String input = inputField.getText();
			System.out.println("输入  "+input);
			String origin = "";

			if (input.contains("$菜单")) {
				apiService.openMenu(0);
				return;
			} else if (input.contains("$exit")) {
				apiService.totalClose();
				return;
			} else if (input.startsWith("$shell:")) {
				// 调用shell
				origin = apiService.shShell(input.replace("$shell:", ""));
			} else {
				// 请求接口
				origin = apiService.chat(input);
			}

			// 解析回调数据
			String analyzed = apiService.analysisStr(origin);
			// 设置文字

			if (apiService.getWsFlag()) {
				apiService.setModelText(analyzed, 0);
			} else {
				smartSetText(label, analyzed);
			}
		}
	}

	/**
	 * 自动换行设置文字
	 * 
	 * @param jLabel
	 * @param longString
	 */
	public void smartSetText(JLabel jLabel, String longString) {
		lablePanel.setBackground(bg);
		StringBuilder sb = new StringBuilder("<html>");
		char[] chars = longString.toCharArray();
		FontMetrics fontMetrics = jLabel.getFontMetrics(jLabel.getFont());
		int start = 0;
		int len = 0;

		while (start + len < longString.length()) {
			while (true) {
				len++;
				if (start + len > longString.length())
					break;
				if (fontMetrics.charsWidth(chars, start, len) > fatherFrame.getWidth()) {
					break;
				}
			}
			sb.append(chars, start, len - 1).append("<br/>");
			start = start + len - 1;
			len = 0;
		}
		sb.append(chars, start, longString.length() - start);

		sb.append("</html>");
		System.out.println(sb.toString());

		jLabel.setText(sb.toString());
	}

	/**
	 * 窗体做正玄浮动
	 */
	@Override
	public void run() {
		// 振幅
		int de = fatherFrame.getHeight() / 80;
		boolean add = true;
		int i = 0;
		while (true) {
			try {
				Thread.sleep(350);
				fatherFrame.setLocation(xnow, ynow - i);

				switchFrame.setLocation(xnow, ynow - i);
				if (add) {
					i++;
				} else {
					i--;
				}
				if (i == de) {
					add = false;
				} else if (i == de * -1) {
					add = true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 窗体渐显
	 *
	 */
	public void magicShow() {
		for (int i = 0; i < 50; i++) {
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
			AWTUtilities.setWindowOpacity(fatherFrame, i * 0.02f);
		}
	}

	/**
	 * 窗体渐隐
	 *
	 */
	public void magicHide() {
		float opacity = 100;
		while (true) {
			if (opacity < 2) {
				System.out.println();
				break;
			}
			opacity = opacity - 2;
			AWTUtilities.setWindowOpacity(fatherFrame, opacity / 100);
			try {
				Thread.sleep(20);
			} catch (Exception e1) {
			}
		}
		// this.hide();
	}

}
