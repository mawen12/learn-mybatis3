package com.mawen.learn.mybatis.builder.annotation;

import java.lang.reflect.Method;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class MethodResolver {

	private final MapperAnnotationBuilder annotationBuilder;
	private final Method method;

	public MethodResolver(MapperAnnotationBuilder annotationBuilder, Method method) {
		this.annotationBuilder = annotationBuilder;
		this.method = method;
	}

	public void resolve() {
		annotationBuilder.parseStatement(method);
	}
}
