package com.github.dmozzy.muster.apilambda;
import com.github.dmozzy.muster.api.MusterService;
import com.github.dmozzy.muster.api.MusterServiceConfiguration;

public interface MusterInternalServices {
	@MusterServiceConfiguration(service="internal", name="PreWarm", idempotency=false)
	public static interface PreWarm extends MusterService<Void, Void> {
		@Override
		Void execute(Void argument);

	}
}
