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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eternity.common.SubSystemNames;
import com.google.gson.Gson;

public abstract class MessageConsumer {

	// /////////////////////////////////////////////////////////////////
	// flyweight support fields and methods
	// There will be an instance of a concrete version of this class/gameId
	// These methods are used to access those instances
	private static final HashMap<SubSystemNames, MessageConsumer> instances = new HashMap<SubSystemNames, MessageConsumer>();
	protected static SubSystemNames subsystemNames;
	
	// this method should only be used for testing 
	
	public static final void resetForTesting(SubSystemNames subsystem) {
		instances.remove(subsystem);
	}

	public static final MessageConsumer getInstance(SubSystemNames subsystem, MessageConsumerFactory messageConsumerFactory, String hostName) {
		MessageConsumer instance = instances.get(subsystem);
		if (instance == null) {
			instance = createInstance(subsystem, messageConsumerFactory, hostName);
		}
		return instance;
	}

	public static final SubSystemNames getSubSystem(String  subSystemId) {
		return subsystemNames.getSubSystem(subSystemId);
	}

	public static final void setSubSystemNames(SubSystemNames subsystemNames) {
		MessageConsumer.subsystemNames = subsystemNames;
	}

	public static final MessageConsumer lookup(SubSystemNames subsystem) {
		return instances.get(subsystem);
	}

	private static synchronized MessageConsumer createInstance(SubSystemNames subsystem, MessageConsumerFactory messageConsumerFactory, String hostName) {
		MessageConsumer instance = instances.get(subsystem);
		if (instance == null) {
			instance = messageConsumerFactory.createMessageConsumer(subsystem);
			instance.setHostName(hostName);
			instance.init();
			MessageConsumer oldInstance = instances.put(subsystem, instance);
			if (oldInstance != null) {
				instance = oldInstance;
			}
		}
		return instance;
	}

	// /////////////////////////////////////////////////////////////////
	// Base message consumer fields and methods
	protected Gson gson;
	protected SubSystemNames subsystem;
	protected RequestFactory requestFactory;
	protected String hostName;
	protected Map<MessageNames, Command> commandRegistry = new HashMap<MessageNames, Command>();
	private boolean ready = false;
	private static Logger log = LoggerFactory.getLogger(MessageConsumer.class);

	protected MessageConsumer(SubSystemNames subsystem) {
		this.subsystem = subsystem;
	}

	/**
	 * populate the commandRegistry define the request and response factories
	 */
	protected abstract void init();


	public boolean isReady(){
		return ready;
	}

	public void setReady(boolean ready){
		this.ready = ready;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Gson getGson() {
		return gson;
	}

	public Response processMessage(Message message) {
		log.debug(message.toString());
		Response response = new Response(gson);
		Command command = commandRegistry.get(message.commandName);

		if (command == null) {
			log.error("unknown command [" + message.commandName + "]");
			response.errors.add("unknown command [" + message.commandName + "]");
			response.setStatusToBadRequest_400();
			return response;
		}

		if(!command.executeAlways() && !ready){//if the server isn't completely started and the command isn't always executed
			log.error("server has not finished starting, message will not be processed - " + this.subsystem);
			response.errors.add("server has not finished starting, message will not be processed - " + this.subsystem);
			response.setStatusToWeMessedUp_500();
			return response;
		}
		//The server has finished starting, or the command is one that's always executed

		Request request = requestFactory.createRequest(message.paramMap);
		request.jsonData = message.jsonData;
		command.execute(request, response);

		return response;
	}

	public Response processMessage(String messageJSON) {
		Message message = gson.fromJson(messageJSON, Message.class);
		return processMessage(message);
	}

	// for handling post requests
	public Response processMessage(String messageJSON, String postData) {
		Message message = gson.fromJson(messageJSON, Message.class);
		message.jsonData = postData;
		return processMessage(message);
	}

	public Response processMessages(ArrayList<Message> messages) {
		StringBuffer responseString = new StringBuffer("[");
		for (Message message: messages) {
			Response response = processMessage(message);
			if (response != null) {
				responseString.append(response.getJSONResponseData());
				responseString.append(',');
			}
		}
		// replace the last , with ] so that the JSON is valid
		responseString.insert(responseString.length() - 1, ']');
		Response response = new Response(gson);
		response.setJSONResponse(responseString.toString());
		return response;
	}
}
