package com.dk.camel_workspace;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Hello world!
 *
 */
public class First {
	public static void main(String[] args) {
		System.out.println("Running tests!");
		JUnitCore.main("com.dk.camel_workspace.AllTests");
//		Result result = JUnitCore.runClasses(AllTests.class);
//		for (Failure failure : result.getFailures()) {
//			System.out.println(failure.toString());
//		}
//		System.out.println(result.wasSuccessful());
	}
}
