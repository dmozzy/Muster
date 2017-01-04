package com.github.dmozzy.muster.apilambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LambdaMusterOrchestrationManager implements MusterOrchestrationManager {

	private final AWSLambdaClient lambdaClient = new AWSLambdaClient();
	private int requestCount = 0;
	private static final long REQUEST_TIMEOUT_AND_SPAWN = 1000l;
	private final String requestReference;
	private final String parentReference;
	private final String currentService;

	private static final ConcurrentHashMap<String, String> serviceAndNametoLambdaName = new ConcurrentHashMap<>();

	ExecutorService executor = Executors.newFixedThreadPool(20);

	private List<Future<?>> futuresToWaitOn = new ArrayList<>();

	public LambdaMusterOrchestrationManager(String parentReference, String requestReference, String currentService) {
		this.parentReference = parentReference;
		this.requestReference = requestReference;
		this.currentService = currentService;
	}

	@Override
	public <C, T, R> Future<R> call(Class<? extends MusterService<T, R>> destination, final T message) {
		Future<R> future = executor.submit(new Callable<R>() {

			@Override
			public R call() throws Exception {
				MusterServiceConfiguration annotation = destination.getAnnotation(com.github.dmozzy.muster.api.MusterServiceConfiguration.class);
				String method = annotation.name();
				String lambda = annotation.service();

				boolean isInternal = "internal".equals(lambda);

				if (!isInternal) {
					log.info("Am invoking {}", destination);
				}
				long start = System.currentTimeMillis();

				final InvokeRequest invokeRequest = new InvokeRequest();
				if (isInternal) {
					lambda = currentService;
				} else {
					invokeRequest.setFunctionName(getLambdaName(lambda));
				}
				ObjectMapper mapper = new ObjectMapper();
				String requestData = null;

				final LambdaMusterRequest lambdaMusterRequest = new LambdaMusterRequest(parentReference,
						requestReference + "." + (requestCount++), method, message);
				requestData = mapper.writeValueAsString(lambdaMusterRequest);

				invokeRequest.setPayload(requestData);

				log.debug("Before invoke {} in {} ms", destination, System.currentTimeMillis() - start);

				List<Future<LambdaMusterResponse<R>>> instancesInProgress = new ArrayList<>();

				while (true) {
					boolean inProgressDetected = false;
					List<Future<LambdaMusterResponse<R>>> newInstancesInProgress = new ArrayList<>();

					for (int i = 0; i < instancesInProgress.size(); i++) {
						long timeout = 0;
						if (i == 0) {
							timeout = REQUEST_TIMEOUT_AND_SPAWN;
						}

						Future<LambdaMusterResponse<R>> future = instancesInProgress.get(i);

						try {
							LambdaMusterResponse<R> lambdaMusterResponse = future.get(timeout, TimeUnit.MILLISECONDS);
							log.debug("After invoke {} in {} ms", destination, System.currentTimeMillis() - start);

							return lambdaMusterResponse.getData();

						} catch (AllreadyInProgressException| RetryableException e ) {
							inProgressDetected = true;
							// means another one is going to return so this is
							// ok.
						} catch (TimeoutException e) {
							// This is ok means we get to go around again
							log.info("Call timed out with timeout {} ms", timeout);
							newInstancesInProgress.add(future);
						}

					}

					if (newInstancesInProgress.size() < 5 && !inProgressDetected) {
						log.info("Am spawning call");
						Future<LambdaMusterResponse<R>> additionalCall = invokeCallInstance(destination, invokeRequest, lambda,
								mapper);
						newInstancesInProgress.add(additionalCall);
					}

					instancesInProgress = newInstancesInProgress;
				}
			}

			private Future<LambdaMusterResponse<R>> invokeCallInstance(
					Class<? extends MusterService<T, R>> destination, final InvokeRequest invokeRequest, final String lambda,
					ObjectMapper mapper) {

				Future<LambdaMusterResponse<R>> future = executor.submit(new Callable<LambdaMusterResponse<R>>() {

					@Override
					public LambdaMusterResponse<R> call() throws Exception {
						invokeRequest.setFunctionName(getLambdaName(lambda));

						
						try {
							InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
							String jsonResponse = new String(invokeResult.getPayload().array());
	
							JavaType type = mapper.getTypeFactory().constructParametrizedType(LambdaMusterResponse.class,
									LambdaMusterResponse.class, getReturnType(destination));
	
							LambdaMusterResponse<R> response = mapper.readValue(jsonResponse, type);
							LambdaMusterResponse.ResponseStatus responseStatus = response.getResponseStatus();
							switch (responseStatus) {
							case IN_PROGRESS:
								throw new AllreadyInProgressException();
							case ERROR:
								throw new DownstreamException(
										"Downstream " + destination.getName() + ">" + response.getErrorMessage());
							default:
								break;
							}
	
						return response;
						} catch (ResourceNotFoundException e) {
							//Refresh the function name cache as one of the functions has likely changed and try again
							serviceAndNametoLambdaName.clear();
							throw new RetryableException();
						}


					}
				});

				return future;

			}
		});
		futuresToWaitOn.add(future);
		return future;
	}

	private <T, R> Class<R> getReturnType(Class<? extends MusterService<T, R>> publicInterface) {
		if (publicInterface != null) {
			Method[] methods = publicInterface.getMethods();
			for (Method method : methods) {
				if (publicInterface.equals(method.getDeclaringClass()) && !method.isBridge()
						&& "execute".equals(method.getName())) {
					if (method.getParameterTypes().length == 1) {
						return (Class<R>) method.getReturnType();
					}
				}
			}
		}
		return null;
	}

	private static synchronized String getLambdaName(String service) {
		String lambdaName = serviceAndNametoLambdaName.get(service);

		if (lambdaName == null) {
			AWSLambdaClient lambdaClient = new AWSLambdaClient();
			ListFunctionsResult lambdas = lambdaClient.listFunctions();
			for (FunctionConfiguration lambda : lambdas.getFunctions()) {
				log.info("Have lambda with name {}", lambda.getFunctionName());

				String[] components = lambda.getFunctionName().split("\\-");
				if (components.length > 1) { // lambda format
												// stack-function-random
					serviceAndNametoLambdaName.put(components[1], lambda.getFunctionName());
				}
			}

		}

		lambdaName = serviceAndNametoLambdaName.get(service);
		log.info("am returning {} for lambda with name {}", lambdaName, service);

		return lambdaName;
	}

	public void waitForCompletion() {

		for (Future<?> future : futuresToWaitOn) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				log.error("Error waiting on future.", e);
			}
		}

	}
}
