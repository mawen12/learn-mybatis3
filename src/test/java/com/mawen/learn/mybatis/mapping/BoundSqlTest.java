package com.mawen.learn.mybatis.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.session.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundSqlTest {

	@Test
	void testHasAdditionalParameter() {
		List<ParameterMapping> params = Collections.emptyList();
		BoundSql boundSql = new BoundSql(new Configuration(), "some sql", params, new Object());

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		boundSql.setAdditionalParameter("map", map);

		Person bean = new Person();
		bean.id = 1;
		boundSql.setAdditionalParameter("person", bean);

		String[] array = new String[]{"User1", "User2"};
		boundSql.setAdditionalParameter("array", array);

		assertFalse(boundSql.hasAdditionalParameter("pet"));
		assertFalse(boundSql.hasAdditionalParameter("pet.name"));

		assertTrue(boundSql.hasAdditionalParameter("map"));
		assertTrue(boundSql.hasAdditionalParameter("map.key1"));
		assertTrue(boundSql.hasAdditionalParameter("map.key2"), "should return true even if the child property does not exists.");

		assertTrue(boundSql.hasAdditionalParameter("person"));
		assertTrue(boundSql.hasAdditionalParameter("person.id"));
		assertTrue(boundSql.hasAdditionalParameter("person.name"), "should return true even if the child property does not exists.");

		assertTrue(boundSql.hasAdditionalParameter("array[0]"));
		assertTrue(boundSql.hasAdditionalParameter("array[99"), "should return true even if the element does not exists.");
	}

	@Test
	void shouldGetAdditionalParameterIsSame() {
		List<ParameterMapping> params = Collections.emptyList();
		BoundSql boundSql = new BoundSql(new Configuration(), "some sql", params, new Object());

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		boundSql.setAdditionalParameter("map", map);

		Person bean = new Person();
		bean.id = 1;
		boundSql.setAdditionalParameter("person", bean);

		String[] array = new String[]{"User1", "User2"};
		boundSql.setAdditionalParameter("array", array);

		// not exists
		assertNull(boundSql.getAdditionalParameter("pet"));
		assertNull(boundSql.getAdditionalParameter("pet.name"));

		// map
		assertEquals(map, boundSql.getAdditionalParameter("map"));
		assertEquals("value1", boundSql.getAdditionalParameter("map.key1"));
		assertNull(boundSql.getAdditionalParameter("map.key2"));

		// object
		assertEquals(bean, boundSql.getAdditionalParameter("person"));
		assertEquals(1, boundSql.getAdditionalParameter("person.id"));

		// array
		assertEquals(array, boundSql.getAdditionalParameter("array"));
		assertEquals("User1", boundSql.getAdditionalParameter("array[0]"));
		assertEquals("User2", boundSql.getAdditionalParameter("array[1]"));
		assertNull(boundSql.getAdditionalParameter("array[99]"));
	}

	public static class Person {
		public Integer id;
	}
}