package com.eternity.common.communication.servlet;

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


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eternity.common.SubSystemNames;
import com.eternity.common.communication.AsyncThreadPool;
import com.eternity.common.message.MessageConsumer;
import com.eternity.common.message.MessageConsumerFactory;
import com.eternity.common.message.Parameter;

public abstract class AsyncDispatch extends HttpServlet implements
		MessageConsumerFactory {
	private static final long serialVersionUID = 42L;
	private static Logger log = LoggerFactory.getLogger(AsyncDispatch.class);
	private String hostName;

	public AsyncDispatch() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			log.error("", e);
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Date date = new Date();
		PrintWriter writer = response.getWriter();
		try {
			String subsystemId = request.getParameter(Parameter.subsystemId.toString());
			String JSON = request.getParameter(Parameter.jsonMessage.toString());

			SubSystemNames subsystem = MessageConsumer.getSubSystem(subsystemId);
			
			if (subsystem != null) {
				MessageConsumer consumer = MessageConsumer.getInstance(subsystem, this, hostName);
				// fire and forget
				AsyncThreadPool.instance.execute(consumer, JSON);
				// always says ok
				writer.println("ok");
			} else {
				String error = "invalid gameId specified [" + subsystemId + "]";
				writer.println(error);
				log.error(error);
			}
		} finally {
			writer.close();
			log.debug("AsynchDispatch completed in : "
					+ (new Date().getTime() - date.getTime()) + "ms");
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
