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


import com.eternity.common.message.MessageConsumer;
import com.eternity.common.message.MessageConsumerFactory;
import com.eternity.common.message.Response;
import com.eternity.common.message.SubSystemMessage;
import com.eternity.socket.server.ConsumerProcessor;
import com.google.gson.Gson;

public class CommonConsumerProcessor extends ConsumerProcessor {
	protected Gson gson;
	protected String hostName;
	protected MessageConsumerFactory messgeConsumerFactory;
	
	public CommonConsumerProcessor(String hostName, MessageConsumerFactory messgeConsumerFactory) {
		super();
		this.hostName = hostName;
		this.messgeConsumerFactory = messgeConsumerFactory;
		this.gson = messgeConsumerFactory.createGson();
	}
	
	@Override
	public String getResponse(String JSON) {
		String retVal = NO_RESPONSE;
		SubSystemMessage message = gson.fromJson(JSON, SubSystemMessage.class);
		
		log.debug("SubSystemMessage="+message);
		
		if (message != null && message.subsystem != null && message.message != null) {
			if (message.subsystem != null) {
				MessageConsumer consumer = MessageConsumer.getInstance(message.subsystem, messgeConsumerFactory, hostName);
				
				log.debug(message.message.toString());
				
				Response response = consumer.processMessage(message.message);
				retVal = response.getJSONResponseData();
				
				log.debug(retVal);
			} else {
				log.error("invalid subsystem specified [" + message.subsystem + "]");
			}
		} else {
			log.error("subsystem and JSON must both be non-null - " + message);
		}
		return retVal;
	}


}
