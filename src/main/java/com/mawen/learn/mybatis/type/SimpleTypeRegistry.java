package com.mawen.learn.mybatis.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class SimpleTypeRegistry {

	private static final Set<Class<?>> SIMPLE_TYPE_SET = new HashSet<>();

	static {
		SIMPLE_TYPE_SET.add(String.class);
		SIMPLE_TYPE_SET.add(Byte.class);
		SIMPLE_TYPE_SET.add(Short.class);
		SIMPLE_TYPE_SET.add(Character.class);
		SIMPLE_TYPE_SET.add(Integer.class);
		SIMPLE_TYPE_SET.add(Long.class);
		SIMPLE_TYPE_SET.add(Float.class);
		SIMPLE_TYPE_SET.add(Double.class);
		SIMPLE_TYPE_SET.add(Boolean.class);
		SIMPLE_TYPE_SET.add(Date.class);
		SIMPLE_TYPE_SET.add(Class.class);
		SIMPLE_TYPE_SET.add(BigInteger.class);
		SIMPLE_TYPE_SET.add(BigDecimal.class);
	}

	public static boolean isSimpleType(Class<?> clazz) {
		return SIMPLE_TYPE_SET.contains(clazz);
	}
}
