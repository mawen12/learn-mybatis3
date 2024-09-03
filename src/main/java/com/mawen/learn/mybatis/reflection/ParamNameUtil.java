package com.mawen.learn.mybatis.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ParamNameUtil {

	public static List<String> getParamNames(Method method) {
		return getParameterNames(method);
	}

	public static List<String> getParamNames(Constructor<?> constructor) {
		return getParameterNames(constructor);
	}

	private static List<String> getParameterNames(Executable executable) {
		return Arrays.stream(executable.getParameters()).map(Parameter::getName).collect(Collectors.toList());
	}

	private ParamNameUtil(){}
}
