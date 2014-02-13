package com.eternity.common.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseError {
	protected Map<String, Object> responseFields = new HashMap<String, Object>();
	protected String jsonResponse = null;
	protected int status = 200;
	protected List<String> errors = new ArrayList<String>();
	
	ResponseError(Map<String, Object> responseFields, String jsonResponse, int status, List<String> errors) {
		this.responseFields = responseFields;
		this.jsonResponse = jsonResponse;
		this.status = status;
		this.errors = errors;
	}
}
