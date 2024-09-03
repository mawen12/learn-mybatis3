package com.mawen.learn.mybatis.reflection.invoker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.mawen.learn.mybatis.reflection.Reflector;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class SetFieldInvoker implements Invoker {

	private final Field field;

	public SetFieldInvoker(Field field) {
		this.field = field;
	}

	@Override
	public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
		try {
			field.set(target, args[0]);
		}
		catch (IllegalAccessException e) {
			if (Reflector.canControlMemberAccessible()) {
				field.setAccessible(true);
				field.set(target, args[0]);
			}
			else {
				throw e;
			}
		}
		return null;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

}
