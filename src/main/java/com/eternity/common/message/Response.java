package com.eternity.common.message;

/*
The MIT License (MIT)

Copyright (c) 2011 Sonjaya Tandon

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. * 
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class Response {
	protected Map<String, Object> responseFields = new HashMap<String, Object>();
	
	protected String jsonResponse = null;
	
	protected int status = 200;
	
	protected Gson gson;
	
	final static String EMPTY_RESPONSE = "";
	
	public List<String> errors = new ArrayList<String>();

	protected Response(Gson gson) {
		this.gson = gson;
	}

	public void setResponseField(ResponseFieldNames responseField, Object value) {
		responseFields.put(responseField.toString(), value);
	}
	
	public int getStatus() {
		return status;
	}

	public void setJSONResponse(String jsonResponse) {
		// sure would be nice if we could verify that this was a well formatted json string
		// because it it wasn't, we could set status to 500 and throw the response in the error so the poor sap
		// using this had something to help debug. But, for now, we will make the poor sap's life just a little bit
		// worse in this case
		this.jsonResponse = jsonResponse; 
	}
	
	public void setJSONResponse(Object value) {
		setJSONResponse(gson.toJson(value));
	}

	public String getJSONResponseData() {
		if (errors.size() > 0 && status == 200) status = 400;
		
		// check to see if we have an error condition
		if (status != 200) {
			// we had errors, return a json version of this object;
			return gson.toJson(new ResponseError(responseFields, jsonResponse, status, errors));
		}
		
		// now, we are either going to return responseFields or jsonResponse
		// check to make sure we don't have values in both.  
		// if we do, someone dun f'd up
		if (responseFields.size() > 0 && jsonResponse != null) {
			status = 500;
			errors.add("There is return information in responseFields and jsonReponse, and yes, the jsonResponse, in this case is supposed to be escaped out, don't try to fix it.");
			return gson.toJson(new ResponseError(responseFields, jsonResponse, status, errors));
		}
		
		// check to see if we are returning fields
		if (responseFields.size() > 0) {
			return gson.toJson(responseFields);
		}
		
		// check to see if we have a jsonResponse
		if (jsonResponse != null) {
			return jsonResponse;
		}
		
		// guess there is nothing to return
		return EMPTY_RESPONSE;
	}

	public void setStatus(int status) {
		// use the helpers over this one
		// I guess if you want to be a tea pot (418), you can use this one
		this.status = status;
	}
	
	// These are helpers for the most expected return codes
	// I don't know about you, but I sure can use a helpful reminder what the codes are
	public void setStatusToBadRequest_400() {
		this.status = 400;
	}
	
	public void setStatusToRequiresAuthorizedClient_401() {
		this.status = 401;
	}
	
	public void setStatusToForbiddenForClient_403() {
		this.status = 403;
	}
	
	public void setStatusToObjectNotFound_404() {
		this.status = 404;
	}
	
	public void setStatusToTimedOut_408() {
		this.status = 408;
	}
	
	public void setStatusToWeMessedUp_500() {
		this.status = 500;
	}
	
	public void setStatusToTryAgainLater_503() {
		this.status = 503;
	}

	public void addErrors(ArrayList<String> errors) {
		this.errors.addAll(errors);
	}

	public void addError(String error) {
		errors.add(error);
	}
}
