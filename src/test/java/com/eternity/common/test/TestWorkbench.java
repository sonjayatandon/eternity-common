package com.eternity.common.test;

import org.junit.Test;

import com.eternity.common.SubSystemNames;
import com.eternity.common.message.MessageConsumer;
import com.eternity.common.message.MessageConsumerFactory;
import com.eternity.common.message.Response;
import com.eternity.reference.ReferenceMessageConsumer;
import com.eternity.reference.json.GsonFactory;
import com.google.gson.Gson;



public class TestWorkbench implements MessageConsumerFactory {

	@Test
	public void testMessageConsumer() {
		MessageConsumer mc = MessageConsumer.getInstance(TestSubSystems.alpha, this, "");
		mc.setReady(true);
		String message = "{\"commandName\":\"HelloWorld\",\"paramMap\": {\"first\":\"1234\",\"second\":\"1\",\"third\":\"12\"}}";
		Response response = mc.processMessage(message);
		assert (response.status == Response.OK);	
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
