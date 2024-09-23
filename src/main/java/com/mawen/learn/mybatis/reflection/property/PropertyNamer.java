package com.mawen.learn.mybatis.reflection.property;

import java.util.Locale;

import com.mawen.learn.mybatis.reflection.ReflectionException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class PropertyNamer {

	public static String methodToProperty(String name) {
		if (name.startsWith("is")) {
			name = name.substring(2);
		}
		else if (name.startsWith("get") || name.startsWith("set")) {
			name = name.substring(3);
		}
		else {
			throw new ReflectionException("Error parsing property name '" + name + "'. Didn't start is 'is', 'get' or 'set'.");
		}

		if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
			name = name.substring(0, 1).toLowerCase(Locale.CHINA) + name.substring(1);
		}

		return name;
	}

	public static boolean isProperty(String name) {
		return isGetter(name) || isSetter(name);
	}

	public static boolean isGetter(String name) {
		return (name.startsWith("get") && name.length() > 3) || (name.startsWith("is") && name.length() > 2);
	}

	public static boolean isSetter(String name) {
		return name.startsWith("set") && name.length() > 3;
	}

	private PropertyNamer() {}
}
