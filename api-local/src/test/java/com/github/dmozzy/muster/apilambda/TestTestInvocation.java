package com.github.dmozzy.muster.apilambda;

import org.junit.Test;

import com.github.dmozzy.muster.apilocal.TestMusterOrchestration;
import com.github.dmozzy.muster.apilocal.TestMusterRegistry;

public class TestTestInvocation {

	@Test
	public void test() {
		TestMusterRegistry registry = TestMusterRegistry.getInstance();
		registry.registerOrchestration(GetBookDetailsFunction.class, new TestMusterOrchestration<>(new GetBookDetailsFunctionImpl()));
		
		TestMusterOrchestration<String, String> testMusterOrchestration = new TestMusterOrchestration<>(new TestMusterFunction1());
		System.out.println(testMusterOrchestration.execute("Hello"));
	}

}
