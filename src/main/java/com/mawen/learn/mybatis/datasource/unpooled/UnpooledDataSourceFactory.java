package com.mawen.learn.mybatis.datasource.unpooled;

import java.util.Properties;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.datasource.DataSourceException;
import com.mawen.learn.mybatis.datasource.DataSourceFactory;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.SystemMetaObject;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

	private static final String DRIVER_PROPERTY_PERFIX = "driver.";
	private static final int DRIVER_PROPERTY_PREFIX_LENGTH = DRIVER_PROPERTY_PERFIX.length();

	protected DataSource dataSource;

	public UnpooledDataSourceFactory() {
		this.dataSource = new UnpooledDataSource();
	}

	@Override
	public void setProperties(Properties properties) {
		Properties driverProperties = new Properties();
		MetaObject metaDataSource = SystemMetaObject.forObject(dataSource);

		for (Object key : properties.keySet()) {
			String propertyName = (String) key;
			if (propertyName.startsWith(DRIVER_PROPERTY_PERFIX)) {
				String propertyValue = properties.getProperty(propertyName);
				driverProperties.setProperty(propertyName.substring(DRIVER_PROPERTY_PREFIX_LENGTH), propertyValue);
			}
			else if (metaDataSource.hasSetter(propertyName)) {
				String value = (String) properties.get(propertyName);
				Object convertedValue = convertValue(metaDataSource, propertyName, value);
				metaDataSource.setValue(propertyName, convertedValue);
			}
			else {
				throw new DataSourceException("Unknown DataSource property: " + propertyName);
			}
		}

		if (driverProperties.size() > 0) {
			metaDataSource.setValue("driverProperties", driverProperties);
		}
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	private Object convertValue(MetaObject metaDataSource, String propertyName, String value) {
		Object convertedValue = value;
		Class<?> targetType = metaDataSource.getSetterType(propertyName);
		if (targetType == Integer.class || targetType == int.class) {
			convertedValue = Integer.valueOf(value);
		}
		else if (targetType == Long.class || targetType == long.class) {
			convertedValue = Long.valueOf(value);
		}
		else if (targetType == Boolean.class || targetType == boolean.class) {
			convertedValue = Boolean.valueOf(value);
		}
		return convertedValue;
	}
}
