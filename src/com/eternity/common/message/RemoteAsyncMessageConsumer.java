package com.eternity.common.message;

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


import com.eternity.common.SubSystemNames;
import com.eternity.socket.client.Client;
import com.google.gson.Gson;

public class RemoteAsyncMessageConsumer extends MessageConsumer {
	private String remoteServerAddress;
	protected Gson gson;

	public RemoteAsyncMessageConsumer(SubSystemNames subsystem, String remoteServerAddress, MessageConsumer localConsumer) {
		super(subsystem);
		this.remoteServerAddress = remoteServerAddress;
		this.gson = localConsumer.getGson();
	}

	@Override
	protected void init() {
	}

	@Override
	public Response processMessage(Message jsonMessage) {
		String message = gson.toJson(new SubSystemMessage(jsonMessage, subsystem));
		Client.getInstance(remoteServerAddress).sendRequest(message);
		return new Response();
	}
}
