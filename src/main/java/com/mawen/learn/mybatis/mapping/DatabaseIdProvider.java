package com.mawen.learn.mybatis.mapping;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public interface DatabaseIdProvider {

	default void setProperties(Properties properties) {
		// NOP
	}

	String getDatabaseId(DataSource dataSource) throws SQLException;
}
