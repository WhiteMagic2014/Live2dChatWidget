package com.magic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

public class AsciiPic {

	public static void createPicFile(String picPath) {
		File file = new File(getFileName(picPath));
		try (OutputStream out = new FileOutputStream(file); Writer writer = new OutputStreamWriter(out);) {
			byte[] pic = createAsciiPic(picPath, 1, 1);
			out.write(pic);
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @Description:生成字符画
	 * @param picPath
	 *            文件路径
	 * @param compressX
	 *            x轴采样率 最高为1 (1:1像素采样)
	 * @param compressY
	 *            y轴采样率 最高为1 (1:1像素采样)
	 * @author: chenhaoyu
	 * @time:Jan 28, 2019 4:12:13 PM
	 */
	public static void createPic(String picPath, int compressX, int compressY) {
		File file = new File(getFileName(picPath));

		compressX = compressX != 0 ? compressX : 3;
		compressY = compressY != 0 ? compressY : 5;

		try (OutputStream out = new FileOutputStream(file); Writer writer = new OutputStreamWriter(out);) {
			byte[] pic = createAsciiPic(picPath, compressX, compressY);
			out.write(pic);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private static String getFileName(String picPath) {

		String[] tempArray = picPath.split("/");
		String filename = tempArray[tempArray.length - 1];
		if (filename.contains(".")) {
			String[] nameArray = filename.split("\\.");
			filename = nameArray[0] + "_asc.txt";
		} else {
			filename += "_asc.txt";
		}
		String finalname = "";
		for (int i = 0; i <= tempArray.length - 2; i++) {
			finalname += tempArray[i] + File.separator;
		}
		finalname += filename;
		return finalname;
	}

	private static byte[] createAsciiPic(String path, int compressX, int compressY) {
		if (AsciiPic.isBlank(path)) {
			return null;
		}
		compressX = compressX != 0 ? compressX : 3;
		compressY = compressY != 0 ? compressY : 5;
		StringBuilder sb = new StringBuilder();
		final String base = "@#&$%*o!;.";// 字符串由复杂到简单
		try {
			final BufferedImage image = ImageIO.read(new File(path));
			for (int y = 0; y < image.getHeight(); y += compressY) {
				for (int x = 0; x < image.getWidth(); x += compressX) {
					final int pixel = image.getRGB(x, y);
					final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
					final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
					final int index = Math.round(gray * (base.length() + 1) / 255);
					// System.out.print(index >= base.length() ? " " :
					// String.valueOf(base.charAt(index)));
					sb.append(index >= base.length() ? " " : String.valueOf(base.charAt(index)));
				}
				// System.out.println();
				sb.append("\n");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return sb.toString().getBytes();
	}

	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

}
