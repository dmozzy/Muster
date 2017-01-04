package com.github.dmozzy.muster.apilambda;
import com.github.dmozzy.muster.api.BaseMusterService;
import com.github.dmozzy.muster.api.MusterOrchestrationManager;

public class MusterInternalServicesImpl {
	public static class PreWarmImpl extends BaseMusterService <Void, Void> implements MusterInternalServices.PreWarm {

		@Override
		public Void doService(Void argument, MusterOrchestrationManager orchestrationManager) throws Exception {
			//Don't do anything
			return null;
		}
	}
}
