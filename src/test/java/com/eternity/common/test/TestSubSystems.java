package com.eternity.common.test;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.eternity.common.SubSystemNames;

public enum TestSubSystems implements SubSystemNames {
	alpha;

	private static Logger log = LogManager.getLogger(TestSubSystems.class);

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

