package com.github.dmozzy.muster.apilocal;
import java.util.HashMap;
import java.util.Map;

import com.github.dmozzy.muster.api.MusterService;

public class TestMusterRegistry {
	private static TestMusterRegistry instance = new TestMusterRegistry();
	
	private Map<Class<? extends MusterService>, TestMusterOrchestration> registry = new HashMap<>();
	
	private TestMusterRegistry() {
		super();
	}
	
	public static TestMusterRegistry getInstance() {
		return instance;
	}
	
	public TestMusterOrchestration getOrchestration(Class<? extends MusterService> musterInterface) {
		return registry.get(musterInterface);
	}

	public void registerOrchestration(Class<? extends MusterService> musterInterface, TestMusterOrchestration orchestration) {
		registry.put(musterInterface,orchestration);
	}
}
