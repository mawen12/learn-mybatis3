package com.mawen.learn.mybatis.cache.impl;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.cache.CacheTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerpetualCacheTest extends CacheTest {

	private Cache cache;

	@Test
	@BeforeEach
	void setup() {
		this.cache = new PerpetualCache("test");
	}

	@Test
	@Override
	public void shouldGetId() {
		assertEquals("test", cache.getId());
	}

	@Test
	@Override
	public void shouldPutObject() {
		cache.putObject("a", "b");

		assertEquals("b", cache.getObject("a"));
		assertEquals(1, cache.getSize());
	}

	@Test
	@Override
	public void shouldGetObject() {
		cache.putObject("a", "b");

		assertEquals("b", cache.getObject("a"));
	}

	@Test
	@Override
	public void shouldRemoveObject() {
		cache.putObject("a", "b");
		assertEquals("b", cache.getObject("a"));
		assertEquals(1, cache.getSize());

		cache.removeObject("a");
		assertNull(cache.getObject("a"));
		assertEquals(0, cache.getSize());
	}

	@Test
	@Override
	public void shouldClear() {
		cache.putObject("a", "b");
		assertEquals("b", cache.getObject("a"));

		cache.clear();
		assertNull(cache.getObject("a"));
		assertEquals(0, cache.getSize());
	}

	@Test
	@Override
	public void shouldGetSize() {
		assertEquals(0, cache.getSize());

		cache.putObject("a", "b");
		assertEquals(1, cache.getSize());

		cache.clear();
		assertEquals(0, cache.getSize());
	}
}