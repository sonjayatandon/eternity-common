package com.eternity.common.communication;

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


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.eternity.common.message.MessageConsumer;
import com.eternity.common.message.Response;

public class AsyncThreadPool {
	protected static Logger log = LogManager.getLogger(AsyncThreadPool.class);
	private ExecutorService pool;

	// singleton
	public static final AsyncThreadPool instance = new AsyncThreadPool();

	private AsyncThreadPool() {
		// TODO : make the size of the thread pool configurable externally?
		this.pool = Executors.newFixedThreadPool(10);
	}

	public void execute(MessageConsumer consumer, String JSON) {

		ExecutionThread thread = new ExecutionThread(consumer, JSON);
		pool.execute(thread);
	}

	private class ExecutionThread implements Runnable {
		private MessageConsumer consumer;
		private String JSON;

		public ExecutionThread(MessageConsumer consumer, String JSON) {
			this.consumer = consumer;
			this.JSON = JSON;
		}

		@Override
		public void run() {
			 Response response = consumer.processMessage(JSON);
			 String result = response.getJSONResponseData();
			 log.debug(result);
		}
	}
}
