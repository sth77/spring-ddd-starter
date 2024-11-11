package com.example.app.infra.restapi;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Configuration to let Jackson work nicely with immutable classes created
 * with Lombok's {@link lombok.Value} and {@link lombok.Builder} annotations.
 */
final class LombokJacksonAnnotationIntrospector extends JacksonAnnotationIntrospector {

	private static final long serialVersionUID = 1L; 
	
	/**
	 * Use empty string for Builder method calls. So we can call builder().value()
	 * instead of builder().withValue()
	 */
	@Override
	public JsonPOJOBuilder.Value findPOJOBuilderConfig(AnnotatedClass ac) {
		if (ac.hasAnnotation(JsonPOJOBuilder.class)) {
			return super.findPOJOBuilderConfig(ac);
		}
		return new JsonPOJOBuilder.Value("build", "");
	}

	@Override
	public Class<?> findPOJOBuilder(AnnotatedClass ac) {
		if (!ac.hasAnnotation(JsonPOJOBuilder.class)) {
			try {
				return Class.forName(ac.getName() + "$" + ac.getRawType().getSimpleName() + "Builder");
			} catch (ClassNotFoundException e) {
				// use default
			}
		}
		return super.findPOJOBuilder(ac);
	}
	
}
