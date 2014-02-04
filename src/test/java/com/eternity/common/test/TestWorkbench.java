package com.eternity.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import static org.junit.Assert.*;

import com.eternity.common.SubSystemNames;
import com.eternity.common.message.MessageConsumer;
import com.eternity.common.message.MessageConsumerFactory;
import com.eternity.common.message.Response;
import com.eternity.reference.ReferenceMessageConsumer;
import com.eternity.reference.json.GsonFactory;
import com.google.gson.Gson;



public class TestWorkbench implements MessageConsumerFactory {
	private static Logger log = LoggerFactory.getLogger(TestWorkbench.class);
	
	@Test
	public void testMessageConsumer() {
		MessageConsumer mc = MessageConsumer.getInstance(TestSubSystems.alpha, this, "");
		mc.setReady(true);
		String message = "{\"commandName\":\"HelloWorld\",\"paramMap\": {\"first\":\"1234\",\"second\":\"1\",\"third\":\"12\"}}";
		log.info(message);
		Response response = mc.processMessage(message);
		assertEquals(response.status, Response.OK);
	}

	@Override
	public MessageConsumer createMessageConsumer(SubSystemNames subsystem) {
		
		return new ReferenceMessageConsumer(subsystem);
	}

	@Override
	public Gson createGson() {
		return GsonFactory.getGson();
	}

}
