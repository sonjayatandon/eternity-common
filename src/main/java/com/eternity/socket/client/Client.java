package com.eternity.socket.client;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eternity.socket.common.Constants;

public class Client {
	private static Logger log = LoggerFactory.getLogger(Client.class);

	private ClientSelectorThread selectorThread;

	private String hostPort;

	// multiton
	private Client(String hostPort) {
		this.hostPort = hostPort;
		startClient();
	}

	private static final ConcurrentMap<String, Client> instances = new ConcurrentHashMap<String, Client>();

	public static final Map<String, Client> getInstanceMap() {
		Map<String, Client> retVal = new HashMap<String, Client>();
		synchronized (instances) {
			retVal.putAll(instances);
		}
		return retVal;
	}

	public static Client getInstance(String hostPort) {
		Client instance = instances.get(hostPort);
		if (instance == null) {
			instance = new Client(hostPort);
			Client oldInstance = instances.putIfAbsent(hostPort, instance);
			if (oldInstance != null) {
				instance = oldInstance;
			}
		}
		return instance;
	}

	private void startClient() {
		InetAddress hostAddress = null;
		try {
			String[] ipAddressAndPort = hostPort.split(":");
			hostAddress = InetAddress.getByName(ipAddressAndPort[0]);
			int port = Integer.parseInt(ipAddressAndPort[1]);
			log.info("starting socket client on " + hostPort);
			selectorThread = new ClientSelectorThread(hostAddress, port, Constants.transmissionBufferSize);
			new Thread(selectorThread).start();
			selectorThread.openConnection();
		} catch (Exception e) {
			log.error("socket client startup failed", e);
			return;
		}

		log.info("started socket client on " + hostPort);
	}

	private void stopClient() {
		log.info("stopping socket client on " + hostPort);

		selectorThread.stopPolling();
		selectorThread.closeConnection();

		log.info("stopped socket client on " + hostPort);
	}

	public static void stopAllClients() {
		for (String key : instances.keySet()) {
			instances.get(key).stopClient();
		}
	}

	public Future<String> sendRequest(String request) {
		log.debug("sending request="+request);
		ResponseHandler handler = new ResponseHandler();
		FutureTask<String> futureTask = new FutureTask<String>(handler);
		handler.setFuture(futureTask);
		selectorThread.send(request, handler);
		return futureTask;
	}

}
