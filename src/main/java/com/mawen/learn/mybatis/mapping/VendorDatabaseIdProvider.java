package com.mawen.learn.mybatis.mapping;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class VendorDatabaseIdProvider implements DatabaseIdProvider {

	private Properties properties;

	@Override
	public String getDatabaseId(DataSource dataSource) throws SQLException {
		if (dataSource == null) {
			throw new NullPointerException("dataSource cannot be null");
		}

		try {
			return getDatabaseName(dataSource);
		}
		catch (Exception e) {
			LogHolder.log.error("Could not get a databaseId from dataSource", e);
		}
		return null;
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	private String getDatabaseName(DataSource dataSource) throws SQLException {
		String productName = getDatabaseProductName(dataSource);
		if (this.properties != null) {
			for (Map.Entry<Object, Object> property : properties.entrySet()) {
				if (productName.contains((String) property.getKey())) {
					return (String) property.getValue();
				}
			}
			return null;
		}
		return productName;
	}

	private String getDatabaseProductName(DataSource dataSource) throws SQLException {
		try (Connection conn = dataSource.getConnection()) {
			DatabaseMetaData metaData = conn.getMetaData();
			return metaData.getDatabaseProductName();
		}
	}

	private static class LogHolder {
		private static final Log log = LogFactory.getLog(VendorDatabaseIdProvider.class);
	}
}
