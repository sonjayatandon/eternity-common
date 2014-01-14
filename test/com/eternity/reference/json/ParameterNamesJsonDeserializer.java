package com.eternity.reference.json;

import java.lang.reflect.Type;

import com.eternity.common.message.ParameterNames;
import com.eternity.reference.Parameters;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ParameterNamesJsonDeserializer implements JsonDeserializer<ParameterNames> {

	@Override
	public ParameterNames deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		ParameterNames retVal = Parameters.getValueOf(arg0.getAsString());
		if (retVal == null) throw new JsonParseException(arg0 + " not a valid parameter");
		return retVal;
	}
	
}
