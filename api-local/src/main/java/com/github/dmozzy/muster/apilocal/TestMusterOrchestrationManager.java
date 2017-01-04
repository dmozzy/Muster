package com.github.dmozzy.muster.apilocal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMusterOrchestrationManager implements MusterOrchestrationManager{

	private final TestMusterRegistry registry;
	
	private List<InvokationFunction> fireAndForgets = new ArrayList<>();
	private List<InvokationFunction> functions = new ArrayList<>();
	private final ConcurrentHashMap<String, Object> resultsMap = new ConcurrentHashMap<>();
	
	ExecutorService executor = Executors.newFixedThreadPool(20);
	
	public TestMusterOrchestrationManager(TestMusterRegistry registry) {
		this.registry = registry;
	}
	
	


	@AllArgsConstructor
	private class InvokationConsumer<T> {
		Consumer<T> consumer;
		T message;
	}
	
	@AllArgsConstructor
	private class InvokationFunction<C,T,R> {
		String handle;
		Class<? extends MusterService<T, R>> destination;
		T message;
	}

	@Override
	public <C, T, R> Future<R> call(Class<? extends MusterService<T, R>> destination, T message) {
		return executor.submit(new Callable<R>() {

			@Override
			public R call() throws Exception {
				TestMusterOrchestration orchestration = registry.getOrchestration(destination);					
				orchestration.getOrchestration().setOrchestrationManager( new TestMusterOrchestrationManager(TestMusterRegistry.getInstance()));
				return (R) orchestration.getOrchestration().execute(message);

			}
		});
	
	}
}
