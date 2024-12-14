package com.bugbycode.trading_app.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.LinkedList;

/**
 * 线程池
 */
public class WorkTaskPool extends ThreadGroup {

	private final Logger logger = LogManager.getLogger(WorkTaskPool.class);
	
	private LinkedList<Runnable> queue;
	
	private boolean isClosed = true;
	
	private int max_thread = 1;
	
	public WorkTaskPool(String name,int max_thread) {
		super(name);
		this.queue = new LinkedList<Runnable>();
		this.max_thread = max_thread;
		this.start();
		logger.info(name + " start.");
	}
	
	public synchronized void add(Runnable task) {
		if(task == null || isClosed) {
			return;
		}
		this.queue.addLast(task);
		this.notifyAllTask();
	}
	
	public synchronized void notifyAllTask() {
		this.notifyAll();
	}

	public synchronized Runnable getTask() {
		while(queue.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				logger.error("WorkTaskPool error.",e);
			}
			if(this.isClosed) {
				throw new RuntimeException("Thread pool closed.");
			}
		}
		return this.queue.removeFirst();
	}
	
	private void start() {
		this.isClosed = false;
		for(int index = 0;index < max_thread;index++) {
			new WorkThread().start();
		}
	}
	
	private class WorkThread extends Thread {

		@Override
		public void run() {
			while(!isClosed) {
				Runnable task = getTask();
				try {
					task.run();
				}catch (Exception e) {
					logger.error("Exception occurred while executing task in thread pool", e);
				}
			}
		}
		
	}
}