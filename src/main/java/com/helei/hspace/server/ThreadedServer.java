package com.helei.hspace.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.helei.hspace.ioc.Container;


public class ThreadedServer implements WebServer {

	private Integer port;
	private Integer numThread;
	private Container container;

	private static class TaskParam {
		public Socket clientSocket;
	}

	private static class ServerTask implements Runnable {

		private BlockingQueue<TaskParam> taskQueue;

		ServerTask(BlockingQueue<TaskParam> taskQueue) {
			this.taskQueue = taskQueue;
		}

		@Override
		public void run() {
			while (true) {
				if (Thread.currentThread().isInterrupted()) {
					return;
				}
				try {
					TaskParam param = this.taskQueue.take();
					System.out.println("Process connection from thread " + Thread.currentThread().getId());
					Socket client = param.clientSocket;
					InputStream input = client.getInputStream();
					OutputStream output = client.getOutputStream();
					HttpRequest req = HttpRequest.parse(input);
					System.out.println(req);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (HttpFormatException e) {
					e.printStackTrace();
				} finally {
				}
			}
		}
	} 

	public ThreadedServer(int port, int numThread, Container container) {
		this.port = port;
		this.numThread = numThread;
		this.container = container;
	}

	@Override
	public void run() throws Exception {
		ServerSocket skt = new ServerSocket(this.port);
		ArrayBlockingQueue<TaskParam> taskQueue = new ArrayBlockingQueue<>(50);
		// producer-consumer model
		// create threadpool
		ExecutorService pool = Executors.newFixedThreadPool(this.numThread);
		for (int i = 0; i < numThread; ++i) {
			pool.execute(new ServerTask(taskQueue));
		}
		while (true) {
			Socket client = skt.accept();
			System.out.println("Accept connection...");
			TaskParam param = new TaskParam();
			param.clientSocket = client;
			taskQueue.add(param);
		}
	}

	@Override
	public void addProcessor(RequestProcessor processor) {
		
	}

}