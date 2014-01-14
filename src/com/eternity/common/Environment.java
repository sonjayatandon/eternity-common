package com.eternity.common;

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

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.apache.naming.NamingContext;

public enum Environment {
	production,
	staging,
	qa,
	local;
	
	private static Logger log = Logger.getLogger(Environment.class);
	private static Environment currentEnvironment = null;
	
	public static Environment get() {
		if (currentEnvironment != null) return currentEnvironment;
		
		try {
			InitialContext initialContext;
			initialContext = new javax.naming.InitialContext();
			NamingContext context = (NamingContext)initialContext.lookup("java:comp/env");
			String env = (String)context.lookup("application_environment");
			try {
				currentEnvironment = Environment.valueOf(env);
			} catch (IllegalArgumentException iae) {
				log.error("enum not found for Environment [" + env + "]; defaulting to local", iae);
				currentEnvironment = local;
			}
			
		} catch (Exception e) {
			log.error("Unable to find application_environment; defaulting to local", e);
			currentEnvironment = local;
		} 
		return currentEnvironment;
	}
	 
}
