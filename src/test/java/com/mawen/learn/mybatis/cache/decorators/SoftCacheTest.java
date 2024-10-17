package com.mawen.learn.mybatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.cache.CacheTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SoftCacheTest extends CacheTest {

	@Mock
	private Cache delegate;

	private Cache cache;

	@BeforeEach
	void setup() {
		this.cache = new SoftCache(delegate);
	}

	@Test
	@Override
	public void shouldGetId() {
		when(delegate.getId()).thenReturn("test");

		assertEquals("test", cache.getId());
	}

	@Test
	@Override
	public void shouldPutObject() {
		ArgumentCaptor<SoftReference<Object>> softReferenceCaptor = ArgumentCaptor.forClass(SoftReference.class);
		doNothing().when(delegate).putObject(any(), softReferenceCaptor.capture());

		cache.putObject("a", "b");

		SoftReference<Object> value = softReferenceCaptor.getValue();
		assertEquals("b", value.get());
	}

	@Test
	@Override
	public void shouldGetObject() {
		when(delegate.getObject(any())).thenReturn(new SoftReference<Object>("b", new ReferenceQueue<>()));

		assertEquals("b", cache.getObject("a"));
	}

	@Test
	@Override
	public void shouldRemoveObject() {
		when(delegate.removeObject(any())).thenReturn(new SoftReference<Object>("b", new ReferenceQueue<>()));

		assertEquals("b", cache.removeObject("a"));
	}

	@Test
	@Override
	public void shouldClear() {
		cache.clear();

		verify(delegate).clear();
	}

	@Test
	@Override
	public void shouldGetSize() {
		when(delegate.getSize()).thenReturn(1);

		assertEquals(1, cache.getSize());
	}
}