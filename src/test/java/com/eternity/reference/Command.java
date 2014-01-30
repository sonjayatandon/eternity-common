package com.eternity.reference;

import org.apache.log4j.Logger;

import com.eternity.common.message.Request;
import com.eternity.common.message.Response;


public abstract class Command implements com.eternity.common.message.Command {
	private static Logger log = Logger.getLogger(Command.class);
	
	@Override
	public void execute(Request request, Response response) {
		try {
			ReferenceRequest newsfeedRequest = (ReferenceRequest) request;
			execute(newsfeedRequest, response);
		} catch (ClassCastException e) {
			log.error("", e);
		}
		
	}
	
	public boolean executeAlways(){
		return false;
	}
	
	public abstract void execute(ReferenceRequest request, Response response);

}
