package com.github.dmozzy.muster.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MusterServiceConfiguration {
	public String name();
	public String service();
	public boolean idempotency() default true;
}
