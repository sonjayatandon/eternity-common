package com.eternity.reference;

import java.util.Map;

import org.apache.log4j.Logger;

import com.eternity.common.SubSystemNames;
import com.eternity.common.message.MessageConsumer;
import com.eternity.common.message.ParameterNames;
import com.eternity.common.message.Request;
import com.eternity.common.message.RequestFactory;
import com.eternity.common.test.TestSubSystems;
import com.eternity.reference.commands.HelloWorld;
import com.eternity.reference.json.GsonFactory;

public class ReferenceMessageConsumer extends MessageConsumer implements RequestFactory {

	private static Logger log = Logger.getLogger(ReferenceMessageConsumer.class);

	public ReferenceMessageConsumer(SubSystemNames subsystem) {
		super(subsystem);
		requestFactory = this;
		gson = GsonFactory.getGson();
		subsystemNames = TestSubSystems.alpha;  // any instance will do
	}
	
	@Override
	protected void init() {
		try {
			commandRegistry.put(Messages.HelloWorld, new HelloWorld());
			
		} catch (Exception e) {
			log.error("init fail", e);
		}
	}

	@Override
	public Request createRequest(Map<ParameterNames, String> paramMap) {
		return new ReferenceRequest(paramMap);
	}
}
