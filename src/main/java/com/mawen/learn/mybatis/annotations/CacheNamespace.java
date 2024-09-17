package com.mawen.learn.mybatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.cache.decorators.LruCache;
import com.mawen.learn.mybatis.cache.impl.PerpetualCache;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheNamespace {

	Class<? extends Cache> implementation() default PerpetualCache.class;

	Class<? extends Cache> eviction() default LruCache.class;

	long flushInterval() default 0;

	int size() default 1024;

	boolean readWrite() default true;

	boolean blocking() default false;

	Property[] properties() default {};
}
