package com.github.dmozzy.muster.api;

public abstract class BaseMusterService<T, R> implements MusterService<T, R> {

	private MusterOrchestrationManager orchestrationManager;

	@Override
	public void setOrchestrationManager(MusterOrchestrationManager orchestrationManager) {
		this.orchestrationManager = orchestrationManager;
	}

	public R execute(T argument) {
		
		try {
			return this.doService(argument,orchestrationManager);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
