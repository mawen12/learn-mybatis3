package com.mawen.learn.mybatis.cache.decorators;

import java.lang.reflect.Constructor;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.cache.CacheTest;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingCacheTest extends CacheTest {

	@Mock
	private Cache delegate;

	@Mock
	private LogFactory logFactory;

	@Mock
	private Log log;

	@Mock
	ClassSupplier<Log> logClass;

	@Mock
	private ConstructorSupplier<Log> constructor;

	private Cache cache;

	@BeforeEach
	void setup() throws Exception {
		when(logClass.getAsClass()).thenReturn(Log.class);
		when(logClass.getAsClass().getConstructor(any())).thenReturn(constructor.getAsConstructor());
		when(logClass.getAsClass().newInstance()).thenReturn(log);

		LogFactory.useCustomLogging(logClass.getAsClass());

		this.cache = new LoggingCache(delegate);
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

	}

	@Test
	@Override
	public void shouldGetObject() {

	}

	@Test
	@Override
	public void shouldRemoveObject() {

	}

	@Test
	@Override
	public void shouldClear() {

	}

	@Test
	@Override
	public void shouldGetSize() {

	}

	public interface ClassSupplier<T> {

		Class<T> getAsClass();
	}

	public interface ConstructorSupplier<T> {

		Constructor<T> getAsConstructor();
	}


}