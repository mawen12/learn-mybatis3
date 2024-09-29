package com.mawen.learn.mybatis.builder;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ParameterExpressionTest {

	@Test
	void simpleProperty() {
		Map<String, String> result = new ParameterExpression("id");
		assertEquals(1, result.size());
		assertEquals("id", result.get("property"));
	}

	@Test
	void propertyWithSpacesInside() {
		Map<String, String> result = new ParameterExpression(" with spaces ");
		assertEquals(1, result.size());
		assertEquals("with spaces", result.get("property"));
	}

	@Test
	void simplePropertyWithOldStyleJdbcType() {
		Map<String, String> result = new ParameterExpression("id:VARCHAR");
		assertEquals(2, result.size());
		assertEquals("id", result.get("property"));
		assertEquals("VARCHAR", result.get("jdbcType"));
	}

	@Test
	void oldStyleJdbcTypeWithExtraWhitespaces() {
		Map<String, String> result = new ParameterExpression(" id :  VARCHAR");
		assertEquals(2, result.size());
		assertEquals("id", result.get("property"));
		assertEquals("VARCHAR", result.get("jdbcType"));
	}

	@Test
	void simplePropertyWithOneAttribute() {
		Map<String, String> result = new ParameterExpression("id,name=value");
		assertEquals(2, result.size());
		assertEquals("id", result.get("property"));
		assertEquals("value", result.get("name"));
	}

	@Test
	void expressionWithOneAttribute() {
		Map<String, String> result = new ParameterExpression("(id.toString()),name=value");
		assertEquals(2, result.size());
		assertEquals("id.toString()", result.get("property"));
		assertEquals("value", result.get("name"));
	}

	@Test
	void simplePropertyWithManyAttributes() {
		Map<String, String> result = new ParameterExpression("id, attr1=val1, attr2=val2, attr3=val3");
		assertEquals(4, result.size());
		assertEquals("id", result.get("property"));
		assertEquals("val1", result.get("attr1"));
		assertEquals("val2", result.get("attr2"));
		assertEquals("val3", result.get("attr3"));
	}

	@Test
	void expressionWithManyAttributes() {
		Map<String, String> result = new ParameterExpression("(id.toString()), attr1=val1, attr2=val2, attr3=val3");
		assertEquals(4, result.size());
		assertEquals("id.toString()", result.get("property"));
		assertEquals("val1", result.get("attr1"));
		assertEquals("val2", result.get("attr2"));
		assertEquals("val3", result.get("attr3"));
	}

	@Test
	void simplePropertyWithOldStyleJdbcTypeAndAttributes() {
		Map<String, String> result = new ParameterExpression("id:VARCHAR, attr1=val1, att2=val2");
		assertEquals(4, result.size());
		assertEquals("id", result.get("property"));
		assertEquals("VARCHAR", result.get("jdbcType"));
		assertEquals("val1", result.get("attr1"));
		assertEquals("val2", result.get("attr2"));
	}

	@Test
	void simplePropertyWithSpaceAndManyAttributes() {
		Map<String, String> result = new ParameterExpression("user name, attr1=val1, attr2=val2, attr3=val3");
		assertEquals(4, result.size());
		assertEquals("user name", result.get("property"));
		assertEquals("val1", result.get("attr1"));
		assertEquals("val2", result.get("attr2"));
		assertEquals("val3", result.get("attr3"));
	}

	@Test
	void shouldIgnoredLeadingAndTrailingSpaces() {
		Map<String, String> result = new ParameterExpression(" id, jdbcType = VARCHAR, attr1 = val1, attr2 = val2 ");
		assertEquals(4, result.size());
		assertEquals("id", result.get("property"));
		assertEquals("VARCHAR", result.get("jdbcType"));
		assertEquals("val1", result.get("attr1"));
		assertEquals("val2", result.get("attr2"));
	}

	@Test
	void invalidOldJdbcTypeFormat() {
		assertThatExceptionOfType(BuilderException.class)
				.isThrownBy(() -> new ParameterExpression("id:"))
				.withMessageContaining("Parsing error in {id:} in position3");
	}

	@Test
	void invalidJdbcTypeOptUsingExpression() {
		assertThatExceptionOfType(BuilderException.class)
				.isThrownBy(() -> new ParameterExpression("(expression)+"))
				.withMessageContaining("Parsing error in {(expression)+} in position 12");
	}
}