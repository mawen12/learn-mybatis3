package com.mawen.learn.mybatis.datasource;

import java.util.Properties;

import javax.sql.DataSource;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface DataSourceFactory {

	void setProperties(Properties properties);

	DataSource getDataSource();
}
