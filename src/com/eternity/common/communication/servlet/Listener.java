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


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.naming.NamingContext;

import com.eternity.common.communication.CommonConsumerProcessor;
import com.eternity.common.message.MessageConsumerFactory;
import com.eternity.socket.client.Client;
import com.eternity.socket.common.WorkerPool;
import com.eternity.socket.server.Server;


/*
 * Starts and stops the socket server with servlet context 
 */
public abstract class Listener implements ServletContextListener, MessageConsumerFactory {
	protected static Logger log = Logger.getLogger(Listener.class);
	private String hostName;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		Server.instance.stopServer();
		Client.stopAllClients();
		WorkerPool.getInstance().shutdown();
		
		// TODO shut down attached registered services
		// e.g. CloudRequestScheduler.getInstance().shutdown();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		int serverPort = 9123;
		// TODO : may want to move this into a globally accessible place
		// for now, it's just used for bootstrapping variables
		InitialContext initialContext;
		String environment = "";
		try {
			initialContext = new javax.naming.InitialContext();
			NamingContext context = (NamingContext) initialContext.lookup("java:comp/env");
			environment = (String) context.lookup("application_environment");
		} catch (NamingException e) {
			log.error("", e);
			environment = "local";
		}

		log.info(hostName + " running in environment: " + environment);

		Properties props = new Properties();

		try {
			props.load(getClass().getResourceAsStream("/" + environment + ".properties"));
			serverPort = Integer.parseInt(props.getProperty("server.port"));
			log.debug(serverPort);
		} catch (Exception e) {
			log.error("unable to parse server.port", e);
		}

		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			log.error("", e);
		}
		
		Server.instance.startServer(serverPort, new CommonConsumerProcessor(hostName, Listener.this));
		
	}
	
}
