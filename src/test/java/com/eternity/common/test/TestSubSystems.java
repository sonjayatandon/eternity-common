package com.eternity.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eternity.common.SubSystemNames;

public enum TestSubSystems implements SubSystemNames {
	alpha;

	private static Logger log = LoggerFactory.getLogger(TestSubSystems.class);

	public static SubSystemNames getValueOf(String subsystemId) {
		TestSubSystems retVal = null;
		
		try {
			retVal = TestSubSystems.valueOf(subsystemId);
		} catch (IllegalArgumentException iae) {
			log.error("enum not found for TestSubSystems [" + subsystemId + "]", iae);
		}
		return retVal;
	}

	@Override
	public SubSystemNames getSubSystem(String subSystemId) {
		return getValueOf(subSystemId);
	}
}

