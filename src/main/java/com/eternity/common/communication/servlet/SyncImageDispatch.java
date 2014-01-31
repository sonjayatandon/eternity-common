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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.eternity.common.SubSystemNames;
import com.eternity.common.message.MessageConsumer;
import com.eternity.common.message.MessageConsumerFactory;
import com.eternity.common.message.Parameter;
import com.eternity.common.message.Response;

public abstract class SyncImageDispatch extends HttpServlet implements MessageConsumerFactory {
	private static final long serialVersionUID = 42L;
	private static Logger log = LogManager.getLogger(SyncDispatch.class);
	private String hostName;
	
	public SyncImageDispatch() {
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Date date = new Date();
		//PrintWriter writer = response.getWriter();

	    OutputStream out = response.getOutputStream();
		try {
			String subsystemId = request.getParameter(Parameter.subsystemId.toString());
			String JSON = request.getParameter(Parameter.jsonMessage.toString());

			SubSystemNames subsystem = MessageConsumer.getSubSystem(subsystemId);

			if (subsystem != null) {
				MessageConsumer consumer = MessageConsumer.getInstance(subsystem, this, hostName);
				Response consumerResponse = consumer.processMessage(JSON);

			    response.setContentType("image/png");

			   	File image = consumerResponse.getImageResponseData();
			   	if(image == null){
			   		String error = "Command does not return an image.  Try another dispatch servlet";
					log.error(error);
			   	}
			    response.setContentLength((int)image.length());

			    FileInputStream in = new FileInputStream(image);

			    byte[] buf = new byte[1024];
			    int count = 0;
			    while ((count = in.read(buf)) >= 0) {
			        out.write(buf, 0, count);
			    }
			    in.close();
			} else {
				String error = "invalid gameId specified [" + subsystemId + "]";
				log.error(error);
			}
		} finally {
		    out.close();
			log.debug("SynchXMLDispatch completed in : " + (new Date().getTime() - date.getTime()) + "ms");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}