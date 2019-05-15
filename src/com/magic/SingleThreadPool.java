package com.magic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 内部静态类 单例实现全局唯一线程池
 * 
 * @author chenhaoyu
 *
 */
public class SingleThreadPool {

	private static class LazyHolder {
		private static final SingleThreadPool INSTANCE = new SingleThreadPool();
	}

	private SingleThreadPool() {
		System.out.println("构造线程池");
		es = Executors.newCachedThreadPool();
		ses = Executors.newScheduledThreadPool(5);// 定长周期线程池 分配5个线程吧
	}

	public static final SingleThreadPool getInstance() {
		return LazyHolder.INSTANCE;
	}

	private ExecutorService es;

	private ScheduledExecutorService ses;

	public ExecutorService threadPool() {
		return es;
	}

	public ScheduledExecutorService scheduledThreadPool() {
		return ses;
	}

	// 关闭所有线程
	public void close() {
		es.shutdownNow();
		ses.shutdownNow();
	}

}
