package com.mawen.learn.mybatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mawen.learn.mybatis.reflection.ReflectionException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class AmbiguousMethodInvoker extends MethodInvoker{

	private final String exceptionMessage;

	public AmbiguousMethodInvoker(Method method, String exceptionMessage) {
		super(method);
		this.exceptionMessage = exceptionMessage;
	}

	@Override
	public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
		throw new ReflectionException(exceptionMessage);
	}
}
