package com.mawen.learn.mybatis;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.datasource.pooled.PooledDataSource;
import com.mawen.learn.mybatis.datasource.unpooled.UnpooledDataSource;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.jdbc.ScriptRunner;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
public class BaseDataTest {

	public static final String BLOG_PROPERTIES = "com/mawen/learn/mybatis/databases/blog/blog-derby.properties";
	public static final String BLOG_DDL = "com/mawen/learn/mybatis/databases/blog/blog-derby-schema.sql";
	public static final String BLOG_DATA = "com/mawen/learn/mybatis/databases/blog/blog-derby-dataload.sql";

	public static final String JPETSTORE_PROPERTIES = "com/mawen/learn/mybatis/databases/jpetstore/jpetstore-hsqldb.properties";
	public static final String JPETSTORE_DDL = "com/mawen/learn/mybatis/databases/jpetstore/jpetstore-hsqldb-schema.sql";
	public static final String JPETSTORE_DATA = "com/mawen/learn/mybatis/databases/jpetstore/jpetstore-hsqldb-dataload.sql";

	public static UnpooledDataSource createUnpooledDataSource(String resource) throws IOException {
		Properties props = Resources.getResourceAsProperties(resource);
		UnpooledDataSource ds = new UnpooledDataSource();
		ds.setDriver(props.getProperty("driver"));
		ds.setUrl(props.getProperty("url"));
		ds.setUsername(props.getProperty("username"));
		ds.setPassword(props.getProperty("password"));
		return ds;
	}

	public static PooledDataSource createPooledDataSource(String resource) throws IOException {
		Properties props = Resources.getResourceAsProperties(resource);
		PooledDataSource ds = new PooledDataSource();
		ds.setDriver(props.getProperty("driver"));
		ds.setUrl(props.getProperty("url"));
		ds.setUsername(props.getProperty("username"));
		ds.setPassword(props.getProperty("password"));
		return ds;
	}

	public static void runScript(DataSource ds, String resource) throws SQLException, IOException {
		try (Connection connection = ds.getConnection()) {
			ScriptRunner runner = new ScriptRunner(connection);
			runner.setAutoCommit(true);
			runner.setStopOnError(false);
			runner.setLogWriter(null);
			runner.setErrorLogWriter(null);
			runScript(runner, resource);
		}
	}

	public static void runScript(ScriptRunner runner, String resource) throws IOException {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			runner.runScript(reader);
		}
	}

	public static DataSource createBlogDataSource() throws IOException, SQLException {
		DataSource ds = createUnpooledDataSource(BLOG_PROPERTIES);
		runScript(ds, BLOG_DDL);
		runScript(ds, BLOG_DATA);
		return ds;
	}

	public static DataSource createJpetsDataSource() throws IOException, SQLException {
		DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
		runScript(ds, JPETSTORE_DDL);
		runScript(ds, JPETSTORE_DATA);
		return ds;
	}
}
