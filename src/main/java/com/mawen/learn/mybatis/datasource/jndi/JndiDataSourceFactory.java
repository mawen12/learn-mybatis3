package com.mawen.learn.mybatis.datasource.jndi;

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mawen.learn.mybatis.datasource.DataSourceException;
import com.mawen.learn.mybatis.datasource.DataSourceFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class JndiDataSourceFactory implements DataSourceFactory {

	public static final String INITIAL_CONTEXT = "initial_context";
	public static final String DATA_SOURCE = "data_source";
	public static final String ENV_PREFIX = "env.";

	private DataSource dataSource;

	@Override
	public void setProperties(Properties properties) {
		try {
			InitialContext initCtx;
			Properties env = getEnvProperties(properties);
			if (env == null) {
				initCtx = new InitialContext();
			}
			else {
				initCtx = new InitialContext(env);
			}

			if (properties.containsKey(INITIAL_CONTEXT) && properties.containsKey(DATA_SOURCE)) {
				Context ctx = (Context) initCtx.lookup(properties.getProperty(INITIAL_CONTEXT));
				this.dataSource = (DataSource) ctx.lookup(properties.getProperty(DATA_SOURCE));
			}
			else if (properties.containsKey(DATA_SOURCE)) {
				this.dataSource = (DataSource) initCtx.lookup(properties.getProperty(DATA_SOURCE));
			}

		}
		catch (NamingException e) {
			throw new DataSourceException("There was an error configuring JndiDataSourceTransactionPool. Cause: " + e, e);
		}
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	private static Properties getEnvProperties(Properties allProps) {
		final String PREFIX = ENV_PREFIX;
		Properties contextProperties = null;
		for (Map.Entry<Object, Object> entry : allProps.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (key.startsWith(PREFIX)) {
				if (contextProperties == null) {
					contextProperties = new Properties();
				}
				contextProperties.put(key.substring(PREFIX.length()), value);
			}
		}
		return contextProperties;
	}
}
