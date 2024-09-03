package com.mawen.learn.mybatis.reflection;

import com.mawen.learn.mybatis.reflection.factory.DefaultObjectFactory;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;
import com.mawen.learn.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.mawen.learn.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class SystemMetaObject {

	public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
	public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(new NullObject(), DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());

	private SystemMetaObject() {}

	private static class NullObject {}

	public static MetaObject forObject(Object object) {
		return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
	}
}
