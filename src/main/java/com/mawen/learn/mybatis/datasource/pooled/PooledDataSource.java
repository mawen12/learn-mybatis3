package com.mawen.learn.mybatis.datasource.pooled;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.datasource.unpooled.UnpooledDataSource;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class PooledDataSource implements DataSource {

	private static final Log log = LogFactory.getLog(PooledDataSource.class);

	private final PoolState state = new PoolState(this);

	private final UnpooledDataSource dataSource;

	protected int poolMaximumActiveConnections = 0;
	protected int poolMaximumIdleConnections = 0;
	protected int poolMaximumCheckoutTime = 20000;
	protected int poolTimeToWait = 20000;
	protected int poolMaximumLocalBadConnectionTolerance = 3;
	protected String poolPingQuery = "NO PING QUERY SET";
	protected boolean poolPingEnabled;
	protected int poolPingConnectionsNotUsedFor;

	private int expectedConnectionTypeCode;

	public PooledDataSource() {
		this.dataSource = new UnpooledDataSource();
	}

	public PooledDataSource(UnpooledDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public PooledDataSource(String driver, String url, String username, String password) {
		this.dataSource = new UnpooledDataSource(driver, url, username, password);
		this.expectedConnectionTypeCode =
	}

	@Override
	public Connection getConnection() throws SQLException {
		return null;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return null;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException(getClass().getName() + " is not a wrapper.");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	@Override
	protected void finalize() throws Throwable {
		forceCloseAll();
		super.finalize();
	}

	protected boolean pingConnection()

	public static Connection unwrapConnection(Connection connection) {
		if (Proxy.isProxyClass(connection.getClass())) {
			InvocationHandler handler = Proxy.getInvocationHandler(connection);
			if (handler instanceof PooledConnection) {
				return handler.getReadConnection();
			}
		}
		return connection;
	}
}
