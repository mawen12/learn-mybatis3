package com.mawen.learn.mybatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface Invoker {

	Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

	Class<?> getType();
}
