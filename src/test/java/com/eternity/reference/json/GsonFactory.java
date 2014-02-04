package com.eternity.reference.json;

import com.eternity.common.SubSystemNames;
import com.eternity.common.message.MessageNames;
import com.eternity.common.message.ParameterNames;
import com.eternity.common.test.SubSystemNamesJsonDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {
	public static Gson getGson() {
		return singleton.gson;
	}
	private static GsonFactory singleton = new GsonFactory();

	private Gson gson;
	private GsonFactory() {
		gson = new GsonBuilder()
		.registerTypeAdapter(ParameterNames.class, new ParameterNamesJsonDeserializer())
		.registerTypeAdapter(MessageNames.class, new MessageNamesJsonDeserializer())
		.registerTypeAdapter(SubSystemNames.class, new SubSystemNamesJsonDeserializer())
		.create();
	}
	

}
