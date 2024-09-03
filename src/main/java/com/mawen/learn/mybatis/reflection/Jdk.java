package com.mawen.learn.mybatis.reflection;

import com.mawen.learn.mybatis.io.Resources;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class Jdk {

	public static final boolean parameterExists;

	static {
		boolean available = false;
		try {
			Resources.classForName("java.lang.reflect.Parameter");
			available = true;
		}
		catch (ClassNotFoundException e) {
			// ignore
		}
		parameterExists = available;
	}

	public static final boolean dateAndTimeApiExists;

	static {
		boolean available = false;
		try {
			Resources.classForName("java.time.Clock");
			available = true;
		}
		catch (ClassNotFoundException e) {
			// ignore
		}
		dateAndTimeApiExists = available;
	}

	public static final boolean optionalExists;

	static {
		boolean available = false;
		try {
			Resources.classForName("java.util.Optional");
			available = true;
		}
		catch (ClassNotFoundException e) {
			// ignore
		}
		optionalExists = available;
	}

	private Jdk() {}
}
