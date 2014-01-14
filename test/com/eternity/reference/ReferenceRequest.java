package com.eternity.reference;

import java.util.Map;

import com.eternity.common.message.ParameterNames;
import com.eternity.common.message.Request;

public class ReferenceRequest extends Request {
	public ReferenceRequest(Map<ParameterNames, String> params) {
		super(params);
	}
}
