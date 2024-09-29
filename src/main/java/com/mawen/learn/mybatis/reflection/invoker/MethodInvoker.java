package com.mawen.learn.mybatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mawen.learn.mybatis.reflection.Reflector;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class MethodInvoker implements Invoker {

	private final Class<?> type;
	private final Method method;

	public MethodInvoker(Method method) {
		this.method = method;

		if (method.getParameters().length == 1) {
			type = method.getParameterTypes()[0];
		}
		else {
			type = method.getReturnType();
		}
	}

	@Override
	public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
		try {
			return method.invoke(target, args);
		}
		catch (IllegalAccessException e) {
			if (Reflector.canControlMemberAccessible()) {
				method.setAccessible(true);
				return method.invoke(target, args);
			}
			else {
				throw e;
			}
		}
	}

	@Override
	public Class<?> getType() {
		return type;
	}
}
