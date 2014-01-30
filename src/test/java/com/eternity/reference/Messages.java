package com.eternity.reference;
import org.apache.log4j.Logger;

import com.eternity.common.message.MessageNames;


public enum Messages implements MessageNames {
	HelloWorld;

	private static Logger log = Logger.getLogger(Messages.class);

	public static Messages getValueOf(String command) {
		Messages retVal = null;
		try {
			retVal = Messages.valueOf(command);
		} catch (IllegalArgumentException iae) {
			log.error("enum not found for Command [" + command + "]", iae);
		}
		return retVal;
	}
}
