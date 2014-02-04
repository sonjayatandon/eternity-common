package com.eternity.reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eternity.common.message.ParameterNames;


public enum Parameters implements ParameterNames {
	first,second,third;

	private static Logger log = LoggerFactory.getLogger(Parameters.class);

	public static Parameters getValueOf(String parameter) {
		Parameters retVal = null;
		try {
			retVal = Parameters.valueOf(parameter);
		} catch (IllegalArgumentException iae) {
			log.error("enum not found for Parameter [" + parameter + "]", iae);
		}
		return retVal;
	}
}
