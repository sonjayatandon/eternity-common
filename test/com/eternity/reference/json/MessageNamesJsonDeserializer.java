package com.eternity.reference.json;

import java.lang.reflect.Type;

import com.eternity.common.message.MessageNames;
import com.eternity.reference.Messages;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class MessageNamesJsonDeserializer implements JsonDeserializer<MessageNames> {

	@Override
	public MessageNames deserialize(JsonElement arg0, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		return Messages.getValueOf(arg0.getAsString());
	}
	
}
