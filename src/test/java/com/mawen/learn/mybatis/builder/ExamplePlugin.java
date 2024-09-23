package com.mawen.learn.mybatis.builder;

import java.util.Properties;

import com.mawen.learn.mybatis.plugin.Interceptor;
import com.mawen.learn.mybatis.plugin.Intercepts;
import com.mawen.learn.mybatis.plugin.Invocation;
import com.mawen.learn.mybatis.plugin.Plugin;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
@Intercepts({})
public class ExamplePlugin implements Interceptor {

	private Properties properties;

	@Override
	public Object interceptor(Invocation invocation) throws Throwable {
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target,this);
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}
}
