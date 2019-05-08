package com.liferay.blade.cli.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Christopher Bryan Boyd
 */
@Retention(RUNTIME)
@Target(FIELD)
public
@interface CollectUsage {
	public boolean censor() default false;

}
