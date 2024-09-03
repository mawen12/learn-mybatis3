package com.mawen.learn.mybatis.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ExceptionUtil {

	public static Throwable unwrapThrowable(Throwable wrapped) {
		Throwable unwrapped = wrapped;
		while (true) {
			if(unwrapped instanceof InvocationTargetException) {
				unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
			}
			else if (unwrapped instanceof UndeclaredThrowableException) {
				unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
			}
			else {
				return unwrapped;
			}
		}
	}

	private ExceptionUtil() {}
}
