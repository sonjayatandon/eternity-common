package com.eternity.socket.server;

/*
The MIT License (MIT)

Copyright (c) 2011 Sonjaya Tandon

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. * 
 */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.eternity.socket.common.Constants;
import com.eternity.socket.common.WorkerPool;

public class Server {
	private static Logger log = Logger.getLogger(Server.class);

	private static final int THREAD_POOL_SIZE = 5;
	
	BlockingQueue<DataEvent> queue;
	ServerSelectorThread selectorThread;
	Consumer[] consumers = new Consumer[THREAD_POOL_SIZE];
	private int port;
	
	private Server() {
		// singleton
	}

	public static Server instance = new Server();

	public void startServer(int port, ConsumerProcessor processor) {
		log.info("starting socket server on port " + port);
		
		this.port = port;
		queue = new LinkedBlockingQueue<DataEvent>();
		selectorThread = new ServerSelectorThread(queue, port, Constants.transmissionBufferSize);
		new Thread(selectorThread).start();

		for (int i = 0; i < THREAD_POOL_SIZE; i++) {
			consumers[i] = new Consumer(queue, selectorThread, processor);
			WorkerPool.getInstance().execute(consumers[i]);
		}

		log.info("started socket server on port " + port);
	}

	public void stopServer() {
		log.info("stopping socket server on port " + port);

		selectorThread.stopPolling();
		for (Consumer consumer : consumers) {
			consumer.stopPolling();
		}

		log.info("stopped socket server on port " + port);
	}
}
