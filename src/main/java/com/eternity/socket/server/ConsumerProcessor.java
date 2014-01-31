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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.eternity.socket.common.Constants;

public abstract class ConsumerProcessor {
	protected static Logger log = LogManager.getLogger(ConsumerProcessor.class);
	
	protected static final String NO_RESPONSE = "";
	
	protected void processEvent(DataEvent dataEvent, ServerSelectorThread serverSelectorThread) {
		log.debug("DataEvent =" + dataEvent);
		try {
			String[] data = dataEvent.processedData.split(Constants.startOfTransmission+"");
			if (data.length != 2) {
				throw new Exception("invalid dataEvent received : DataEvent =" + dataEvent);
			}
			String messageHeader = data[0];
			String messageBody   = data[1].substring(0, data[1].length() - 1);

			String responseBody = getResponse(messageBody);			
			sendResponse(messageHeader, responseBody, dataEvent, serverSelectorThread);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public abstract String getResponse(String messgeBody);
	
	public void sendResponse(String messageHeader, String messageBody, DataEvent dataEvent, ServerSelectorThread serverSelectorThread) {
		if (!messageBody.equals(NO_RESPONSE)) {
			String response = messageHeader + Constants.startOfTransmission + messageBody + Constants.endOfTransmission; 
			log.debug("responding with [" + response + "]");
			serverSelectorThread.send(dataEvent.socketChannel, response.getBytes());
		}
	}
}
