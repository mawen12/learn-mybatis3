package com.mawen.learn.mybatis.scripting.xmltags;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.builder.BuilderException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class ExpressionEvaluator {

	public boolean evaluateBoolean(String expression, Object parameterObject) {
		Object value = OgnlCache.getValue(expression, parameterObject);
		if (value instanceof Boolean) {
			return (Boolean) value;
		}

		if (value instanceof Number) {
			return new BigDecimal(String.valueOf(value)).compareTo(BigDecimal.ZERO) != 0;
		}

		return value != null;
	}

	public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
		return evaluateIterable(expression, parameterObject, false);
	}

	public Iterable<?> evaluateIterable(String expression, Object parameterObject, boolean nullable) {
		Object value = OgnlCache.getValue(expression, parameterObject);
		if (value == null) {
			if (nullable) {
				return null;
			}
			else {
				throw new BuilderException("The expression '" + expression + "' evaluated to a");
			}
		}

		if (value instanceof Iterable) {
			return (Iterable<?>) value;
		}

		if (value.getClass().isArray()) {
			int size = Array.getLength(value);
			List<Object> answer = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				Object o = Array.get(value, i);
				answer.add(o);
			}
			return answer;
		}

		if (value instanceof Map) {
			return ((Map)value).entrySet();
		}

		throw new BuilderException("Error evaluating expression '" + expression + "'. Return value (" + value + ") was not iterable.");
	}
}
