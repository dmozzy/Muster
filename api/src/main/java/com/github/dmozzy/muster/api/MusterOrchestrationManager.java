package com.github.dmozzy.muster.api;

import java.util.concurrent.Future;

public interface MusterOrchestrationManager {
	<C,T,R> Future<R> call(Class<? extends MusterService<T, R>>  destination,T message);

	
}
