package com.magic.mail;

/**
 * 邮箱统一关闭方法
 * 
 * @author chenhaoyu
 *
 */
public interface MailInterface {

	/**
	 * 统一的关闭方法
	 */
	public void close();

	/**
	 * 定时检查方法
	 */
	public void check();
}
