package com.mawen.learn.mybatis.builder;

import java.util.List;
import java.util.Properties;

import com.mawen.learn.mybatis.reflection.factory.DefaultObjectFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public class ExampleObjectFactory extends DefaultObjectFactory {

	private Properties properties;

	@Override
	public <T> T create(Class<T> type) {
		return super.create(type);
	}

	@Override
	public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
		return super.create(type, constructorArgTypes, constructorArgs);
	}

	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}
}
