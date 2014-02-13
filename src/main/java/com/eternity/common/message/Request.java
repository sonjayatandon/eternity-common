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


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Request {
	private Map<ParameterNames, String> params = new HashMap<ParameterNames, String>();
	protected ArrayList<String> errors = new ArrayList<String>();
	protected String jsonData;
	final protected Gson gson;

	protected Request(Map<ParameterNames, String> params, Gson gson) {
		this.params = params;
		this.gson = gson;
	}

	public boolean isValid() {
		return errors.size() == 0;
	}

	public String getParam(ParameterNames paramName) {
		String paramValue = (params == null)?null:params.get(paramName);
		if (paramValue == null) {
			errors.add(paramName + " is a required parameter");
		}
		return paramValue;
	}

	public String getOptionalParam(ParameterNames paramName) {
		if (params == null) return null;
		return params.get(paramName);
	}

	public String setParam(ParameterNames paramName, String paramValue) {
		return params.put(paramName, paramValue);
	}

	public Integer getParamAsInteger(ParameterNames paramName) {
		String paramValue = getParam(paramName);
		try {
			if (paramValue != null) {
				return Integer.valueOf(getParam(paramName));
			}
		} catch (NumberFormatException e) {
			errors.add(paramName + " must be a number.  The value passed in was: " + paramValue);
		}
		return null;
	}

	public Integer getOptionalParamAsInteger(ParameterNames paramName) {
		String paramValue = getOptionalParam(paramName);
		try {
			if (paramValue != null) {
				return Integer.valueOf(getParam(paramName));
			}
		} catch (NumberFormatException e) {
			errors.add(paramName + " must be a number.  The value passed in was: " + paramValue);
		}
		return null;
	}

	public Boolean getParamAsBoolean(ParameterNames paramName) {
		String paramValue = getParam(paramName);
		return new Boolean(paramValue);
	}

	public Boolean getOptionalParamAsBoolean(ParameterNames paramName) {
		String paramValue = getOptionalParam(paramName);
		return new Boolean(paramValue);
	}

	public Date getParamAsDate(ParameterNames paramName) {
		String paramValue = getParam(paramName);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date retVal = null;
		try {
			if (paramValue != null) {
				retVal = dateFormat.parse(params.get(paramName));
			}
		} catch (ParseException e) {
			errors.add(paramName + " must be a date of format yyyy-MM-dd HH:mm:ss.  The value passed in was: " + paramValue);
		}
		return retVal;
	}

	public Date getOptionalParamAsDate(ParameterNames paramName) {
		String paramValue = getOptionalParam(paramName);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date retVal = null;
		try {
			if (paramValue != null) {
				retVal = dateFormat.parse(params.get(paramName));
			}
		} catch (ParseException e) {
			errors.add(paramName + " must be a date of format yyyy-MM-dd HH:mm:ss.  The value passed in was: " + paramValue);
		}
		return retVal;
	}

	public long getParamAsLong(ParameterNames paramName) {
		String paramValue = getParam(paramName);
		long retVal = 0;
		try {
			retVal = Long.valueOf(paramValue);
		} catch (NumberFormatException e) {
			errors.add(paramName + " must be parsable as a Long.  The value passed in was: " + paramValue);
		}
		return retVal;
	}

	public long getOptionalParamAsLong(ParameterNames paramName) {
		String paramValue = getOptionalParam(paramName);
		long retVal = 0;
		if (paramValue != null) { // ignore nulls as this is optional
			try {
				retVal = Long.valueOf(paramValue);
			} catch (NumberFormatException e) {
				errors.add(paramName + " must be parsable as a Long.  The value passed in was: " + paramValue);
			}
		}
		return retVal;
	}

	public ArrayList<String> getErrors() {
		return errors;
	}
	
	public String getPostData() {
		return jsonData;
	}

	public <T> T getPostData(Class<T> klass) {
		try {
			T value = gson.fromJson(getPostData(), klass);
			
			if (value == null) {
				this.errors.add("No JSON data passed.");
			}
			
			return value;
		}
		catch (JsonSyntaxException e) {
			this.errors.add("Malformed JSON data passed: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	public Map<ParameterNames, String> getCopyOfParamData(){
		return new HashMap<ParameterNames, String>(params);
	}
}
