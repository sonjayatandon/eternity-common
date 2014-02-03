package com.eternity.common.test;

import java.lang.reflect.Type;

import com.eternity.common.SubSystemNames;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class SubSystemNamesJsonDeserializer implements JsonDeserializer<SubSystemNames> {
	
	@Override
	public SubSystemNames deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		return TestSubSystems.getValueOf(arg0.getAsString());
	}


}
