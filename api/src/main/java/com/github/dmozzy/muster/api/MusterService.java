package com.github.dmozzy.muster.api;

public interface MusterService<T,R> {
	void setOrchestrationManager(MusterOrchestrationManager orchestrationManager);
	
	R doService (T argument, MusterOrchestrationManager orchestrationManager) throws Exception;
	
	R execute(T argument) throws Exception;
}
