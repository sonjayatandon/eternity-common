package com.eternity.reference;

import java.util.Map;

import com.eternity.common.message.ParameterNames;
import com.eternity.common.message.Request;
import com.google.gson.Gson;

public class ReferenceRequest extends Request {
	public ReferenceRequest(Map<ParameterNames, String> params, Gson gson) {
		super(params, gson);
	}
}
