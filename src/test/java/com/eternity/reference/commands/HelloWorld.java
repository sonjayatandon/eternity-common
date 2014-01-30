package com.eternity.reference.commands;
import com.eternity.common.message.Response;
import com.eternity.reference.Command;
import com.eternity.reference.ReferenceRequest;

public class HelloWorld extends Command {

	@Override
	public void execute(ReferenceRequest request, Response response) {
		System.out.println("Hello, world!");
	}


}
