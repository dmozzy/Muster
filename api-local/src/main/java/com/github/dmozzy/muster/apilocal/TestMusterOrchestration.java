package com.github.dmozzy.muster.apilocal;
import com.github.dmozzy.muster.api.MusterService;

import lombok.Getter;

@Getter
public class TestMusterOrchestration<T,R> {
	
	private MusterService<T,R> orchestration;

	public TestMusterOrchestration(MusterService<T,R> orchestration) {
		this.orchestration = orchestration;
		TestMusterOrchestrationManager orchestrationManager = new TestMusterOrchestrationManager(TestMusterRegistry.getInstance());
		orchestration.setOrchestrationManager(orchestrationManager);
	}
	
	
	public R execute(T t) {
		try {
			return orchestration.execute(t);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
}
