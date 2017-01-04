package com.github.dmozzy.muster.apilambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MusterLambdaHandler<T, R> {
	private static final String PRE_WARM_COUNT = "PreWarmCount";
	private static final String SERVICE_CLASSES = "ServiceClasses";
	private MusterService<T, R> orchestration;
	private static Map<String, PublicIntferfaceReference> functionHandlers = new HashMap<>();
	private static AmazonDynamoDBClient dynamoClient = new AmazonDynamoDBClient();
	private long lastPrewarm = -1;

	static {
		String serviceClasses = System.getenv(SERVICE_CLASSES);
		if (serviceClasses == null) {
			throw new IllegalArgumentException("No ServiceClass environment variable specified.");
		}

		for (String serviceClass : serviceClasses.split("\\,")) {
			try {
				Class<?> handler = Class.forName(serviceClass);
				populateHandlerConfiguration(handler);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (functionHandlers.size() == 0) {
			throw new IllegalStateException("No handlers could be created");
		}

	}

	private static void populateHandlerConfiguration(Class<?> handler) {
		Class<?> publicInterface = getPublicInterface(handler);
		if (publicInterface != null) {
			Method[] methods = publicInterface.getMethods();
			for (Method method : methods) {
				if (publicInterface.equals(method.getDeclaringClass()) && !method.isBridge()
						&& "execute".equals(method.getName())) {
					if (method.getParameterTypes().length == 1) {
						MusterServiceConfiguration annotation = publicInterface.getAnnotation(com.github.dmozzy.muster.api.MusterServiceConfiguration.class);
						functionHandlers.put(annotation.name(),
								new PublicIntferfaceReference((Class<? extends MusterService<?, ?>>) handler,
										method.getParameterTypes()[0], method.getReturnType(), annotation.service(),
										annotation.name(), annotation.idempotency()));
					}
				}
			}
		}
		Class<?>[] classes = handler.getClasses();
		for (Class<?> clazz : classes) {
			populateHandlerConfiguration(clazz);
		}
	}

	private static Class<?> getPublicInterface(Class<?> clazz) {
		for (Class<?> iface : clazz.getInterfaces()) {
			if (iface.isAnnotationPresent(com.github.dmozzy.muster.api.MusterServiceConfiguration.class)) {
				return iface;
			}
		}
		return null;
	}


	public LambdaMusterResponse<R> lambdaEntry(LambdaMusterRequest<T> request, Context context) {
		LambdaMusterResponse<R> lambdaMusterResponse = new LambdaMusterResponse<>();
		log.info("Invocation");		
		long start = System.currentTimeMillis();
		ObjectMapper objectMapper = new ObjectMapper();

		if (request.getRequestReference() == null) {
			log.info("No requestReferenceProvided, could be ping");
			if (System.currentTimeMillis()-lastPrewarm > 60000 && System.getenv(PRE_WARM_COUNT) != null) {
				log.info("Am triggering prewarm calls");
				lastPrewarm = System.currentTimeMillis();
				
				String[] components = context.getFunctionName().split("\\-");
				LambdaMusterOrchestrationManager orchestrationManager = new LambdaMusterOrchestrationManager(
						"PreWarm", "PreWarm", components[1]);
				for(int i = 0; i<Integer.parseInt(System.getenv(PRE_WARM_COUNT)); i++) {
					orchestrationManager.call(MusterInternalServices.PreWarm.class, null);
				}
				orchestrationManager.waitForCompletion();
			}
			lambdaMusterResponse.setResponseStatus(LambdaMusterResponse.ResponseStatus.IGNORED);
			lambdaMusterResponse.setErrorMessage("Ignored, am assuming is a keep warm message");
			return lambdaMusterResponse;
		}

		String orchestrationName = request.getMethod();
		if (orchestrationName == null) {
			lambdaMusterResponse.setResponseStatus(LambdaMusterResponse.ResponseStatus.ERROR);
			lambdaMusterResponse.setErrorMessage("No method attribute provided.");
			return lambdaMusterResponse;
		}

		log.info("Running orchestration " + orchestrationName);

		DynamoMusterCall dynamoOrchestration = new DynamoMusterCall();

		String parentReference = request.getParentReference();
		if (parentReference == null) {
			parentReference = request.getRequestReference();
		}
		
		boolean isIdempotent = false;
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoClient);

		try {

			dynamoOrchestration.setRequestReference(request.getRequestReference());
			dynamoOrchestration.setParentReference(parentReference);
			dynamoOrchestration.setRequestMessage(objectMapper.writeValueAsString(request.getData()));
			dynamoOrchestration.setRequestDate(System.currentTimeMillis());

			Set<String> keySet = functionHandlers.keySet();
			for (String key : keySet) {
				log.debug("Have orchestration configured  " + key);
			}

			PublicIntferfaceReference publicIntferfaceReference = functionHandlers.get(orchestrationName);
			isIdempotent = publicIntferfaceReference.idempotencyEnabled;

			
			if (isIdempotent) {

				dynamoOrchestration.setService(publicIntferfaceReference.getService());
				dynamoOrchestration.setName(publicIntferfaceReference.getName());

				DynamoMusterCall existingDynamoOrchestration = mapper.load(DynamoMusterCall.class,
						request.getRequestReference());

				if (existingDynamoOrchestration != null
						&& DynamoMusterCall.STATUS_SUCCESS.equals(existingDynamoOrchestration.getStatus())) {
					log.warn("Duplicate message {} detected, am returning from cached result",
							request.getRequestReference());
					lambdaMusterResponse.setResponseStatus(LambdaMusterResponse.ResponseStatus.COMPLETE_DUPLICATE);
					lambdaMusterResponse.setData((R) objectMapper.readValue(existingDynamoOrchestration.getResponseMessage(),
							publicIntferfaceReference.getResponseType()));
					return lambdaMusterResponse;
				}

				if (existingDynamoOrchestration != null
						&& DynamoMusterCall.STATUS_PENDING.equals(existingDynamoOrchestration.getStatus())
						&& System.currentTimeMillis() - existingDynamoOrchestration.getRequestDate() < 10000l) {
					log.warn("In Progress message {} detected, am returning in progress result",
							request.getRequestReference());
					lambdaMusterResponse.setResponseStatus(LambdaMusterResponse.ResponseStatus.IN_PROGRESS);
					lambdaMusterResponse.setErrorMessage("In Progress detected with timing of " + (System.currentTimeMillis() - existingDynamoOrchestration.getRequestDate()) + " ms start ago");
					return lambdaMusterResponse;
				}
				
				dynamoOrchestration.setStatus(DynamoMusterCall.STATUS_PENDING);
				mapper.save(dynamoOrchestration);
			}
			
			Class<? extends MusterService<T, R>> handler = (Class<? extends MusterService<T, R>>) publicIntferfaceReference
					.getHandlerClass();

			this.orchestration = (MusterService<T, R>) handler.newInstance();

			LambdaMusterOrchestrationManager orchestrationManager = new LambdaMusterOrchestrationManager(
					parentReference, request.getRequestReference(), publicIntferfaceReference.getService());

			this.orchestration.setOrchestrationManager(orchestrationManager);

			R responseObject;
			log.info("Request data is {} ", request.getData());
			if (String.class.equals(publicIntferfaceReference.getRequestParameter())) {
				responseObject = orchestration.execute((T) request.getData());
			} else {
				Class<T> requestParameter = (Class<T>) publicIntferfaceReference.getRequestParameter();
				T requestParameterObject = requestParameter.newInstance();
				BeanUtils.populate(requestParameterObject, (Map)request.getData());
				responseObject = orchestration.execute(requestParameterObject);
			}

			String responseData = objectMapper.writeValueAsString(responseObject);
			if(isIdempotent) {
				dynamoOrchestration.setStatus(DynamoMusterCall.STATUS_SUCCESS);
				dynamoOrchestration.setResponseMessage(responseData);
				dynamoOrchestration.setTiming(System.currentTimeMillis() - start);
				mapper.save(dynamoOrchestration);
			}
			orchestrationManager.waitForCompletion();
			lambdaMusterResponse.setResponseStatus(LambdaMusterResponse.ResponseStatus.COMPLETE);
			lambdaMusterResponse.setData(responseObject);
			return lambdaMusterResponse;
		} catch (Exception e) {
			log.error("Exception running orchestration", e);
			if(isIdempotent) {
				dynamoOrchestration.setStatus(DynamoMusterCall.STATUS_FAILED);
				dynamoOrchestration.setErrorMessage(e.getMessage());
				dynamoOrchestration.setTiming(System.currentTimeMillis() - start);
				mapper.save(dynamoOrchestration);
			}
			lambdaMusterResponse.setResponseStatus(LambdaMusterResponse.ResponseStatus.ERROR);
			lambdaMusterResponse.setErrorMessage("Error running orchestration " + request.getRequestReference() + ", please retry.");
			return lambdaMusterResponse;			
		}

	}

	public R execute(T t) {
		try {
			return orchestration.execute(t);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	@AllArgsConstructor
	@Getter
	private static class PublicIntferfaceReference {
		private Class<? extends MusterService<?, ?>> handlerClass;
		private Class<?> requestParameter;
		private Class<?> responseType;
		private String service;
		private String name;
		private boolean idempotencyEnabled;
	}

}
