package com.mawen.learn.mybatis.cache;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public abstract class CacheTest {

	public abstract void shouldGetId();

	public abstract void shouldPutObject();

	public abstract void shouldGetObject();

	public abstract void shouldRemoveObject();

	public abstract void shouldClear();

	public abstract void shouldGetSize();

}