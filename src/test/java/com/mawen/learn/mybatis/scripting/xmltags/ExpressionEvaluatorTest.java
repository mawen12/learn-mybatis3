package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.HashMap;

import com.mawen.learn.mybatis.domain.blog.Author;
import com.mawen.learn.mybatis.domain.blog.Section;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvaluatorTest {

	public ExpressionEvaluator evaluator = new ExpressionEvaluator();

	@Test
	void shouldCompareStringsReturnType() {
		boolean value = evaluator.evaluateBoolean("username == 'cbegin'",
				new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));

		assertTrue(value);
	}

	@Test
	void shouldCompareStringsReturnFalse() {
		boolean value = evaluator.evaluateBoolean("username == 'norm'",
				new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));

		assertFalse(value);
	}

	@Test
	void shouldReturnTrueIfNotNull() {
		boolean value = evaluator.evaluateBoolean("username",
				new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));

		assertTrue(value);
	}

	@Test
	void shouldReturnFalseIfNull() {
		boolean value = evaluator.evaluateBoolean("password",
				new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));

		assertFalse(value);
	}

	@Test
	void shouldReturnTrueIfNotZero() {
		boolean value = evaluator.evaluateBoolean("id",
				new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));

		assertTrue(value);
	}

	@Test
	void shouldReturnFalseIfZero() {
		boolean value = evaluator.evaluateBoolean("id",
				new Author(0, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));

		assertFalse(value);
	}

	@Test
	void shouldReturnFalseIfZeroWithScale() {
		class Bean {
			public double d = 0.0d;
		}

		assertFalse(evaluator.evaluateBoolean("d", new Bean()));
	}

	@Test
	void shouldIterateOverIterable() {
		final HashMap<String, String[]> parameterObject = new HashMap<String, String[]>(){{
			put("array", new String[]{"1", "2", "3"});
		}};

		final Iterable<?> iterable = evaluator.evaluateIterable("array", parameterObject);
		int i = 0;
		for (Object o : iterable) {
			assertEquals(String.valueOf(++i), o);
		}
	}
}