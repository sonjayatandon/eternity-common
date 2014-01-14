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

import org.apache.log4j.Logger;

public class Consumer implements Runnable {
	private static Logger log = Logger.getLogger(Consumer.class);

	private BlockingQueue<DataEvent> queue;
	// for responding to an event
	private ServerSelectorThread selectorThread;
	private boolean poll = true;
	private ConsumerProcessor processor;

	public Consumer(BlockingQueue<DataEvent> queue, ServerSelectorThread selectorThread, ConsumerProcessor processor) {
		this.queue = queue;
		this.selectorThread = selectorThread;
		this.processor = processor;
	}

	@Override
	public void run() {
		while (poll) {
			try {
				DataEvent dataEvent = queue.take();
				processor.processEvent(dataEvent, selectorThread);
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}
	}

	public void stopPolling() {
		this.poll = false;
	}
}