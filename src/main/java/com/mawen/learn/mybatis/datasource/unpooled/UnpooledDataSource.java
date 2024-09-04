package com.mawen.learn.mybatis.datasource.unpooled;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.io.Resources;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class UnpooledDataSource implements DataSource {

	private static ConcurrentMap<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

	static {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			registeredDrivers.put(driver.getClass().getName(), driver);
		}
	}

	private ClassLoader driverClassLoader;
	private Properties driverProperties;

	private String driver;
	private String url;
	private String username;
	private String password;

	private Boolean autoCommit;
	private Integer defaultTransactionIsolationLevel;
	private Integer defaultNetworkTimeout;

	public UnpooledDataSource() {}

	public UnpooledDataSource(String driver, String url, String username, String password) {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public UnpooledDataSource(String driver, String url, Properties properties) {
		this.driver = driver;
		this.url = url;
		this.driverProperties = properties;
	}

	public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
		this.driverClassLoader = driverClassLoader;
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
		this.driverClassLoader = driverClassLoader;
		this.driver = driver;
		this.url = url;
		this.driverProperties = driverProperties;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return doGetConnection(username, password);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return doGetConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return DriverManager.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		DriverManager.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		DriverManager.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
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

	public ClassLoader getDriverClassLoader() {
		return driverClassLoader;
	}

	public void setDriverClassLoader(ClassLoader driverClassLoader) {
		this.driverClassLoader = driverClassLoader;
	}

	public Properties getDriverProperties() {
		return driverProperties;
	}

	public void setDriverProperties(Properties driverProperties) {
		this.driverProperties = driverProperties;
	}

	public synchronized String getDriver() {
		return driver;
	}

	public synchronized void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public Integer getDefaultTransactionIsolationLevel() {
		return defaultTransactionIsolationLevel;
	}

	public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
		this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
	}

	public Integer getDefaultNetworkTimeout() {
		return defaultNetworkTimeout;
	}

	public void setDefaultNetworkTimeout(Integer defaultNetworkTimeout) {
		this.defaultNetworkTimeout = defaultNetworkTimeout;
	}

	private Connection doGetConnection(String username, String password) throws SQLException {
		Properties props = new Properties();
		if (driverProperties != null) {
			props.putAll(driverProperties);
		}

		if (username != null) {
			props.setProperty("user", username);
		}

		if (password != null) {
			props.setProperty("password", password);
		}

		return doGetConnection(props);
	}

	private Connection doGetConnection(Properties properties) throws SQLException {
		initializeDriver();
		Connection connection = DriverManager.getConnection(url, properties);
		configureConnection(connection);
		return connection;
	}

	private synchronized void initializeDriver() throws SQLException {
		if (!registeredDrivers.containsKey(driver)) {
			Class<?> driverType;
			try {
				if (driverClassLoader != null) {
					driverType = Class.forName(driver, true, driverClassLoader);
				}
				else {
					driverType = Resources.classForName(driver);
				}

				Driver driverInstance = (Driver) driverType.getDeclaredConstructor().newInstance();
				DriverManager.registerDriver(driverInstance);
				registeredDrivers.put(driver, driverInstance);
			}
			catch (Exception e) {
				throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
			}
		}
	}

	private void configureConnection(Connection conn) throws SQLException {
		if (defaultNetworkTimeout != null) {
			conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), defaultNetworkTimeout);
		}

		if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
			conn.setAutoCommit(autoCommit);
		}

		if (defaultTransactionIsolationLevel != null) {
			conn.setTransactionIsolation(defaultTransactionIsolationLevel);
		}
	}

	private static class DriverProxy implements Driver {

		private Driver driver;

		DriverProxy(Driver driver) {
			this.driver = driver;
		}

		@Override
		public Connection connect(String url, Properties info) throws SQLException {
			return this.driver.connect(url, info);
		}

		@Override
		public boolean acceptsURL(String url) throws SQLException {
			return this.driver.acceptsURL(url);
		}

		@Override
		public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
			return this.driver.getPropertyInfo(url, info);
		}

		@Override
		public int getMajorVersion() {
			return this.driver.getMajorVersion();
		}

		@Override
		public int getMinorVersion() {
			return this.driver.getMinorVersion();
		}

		@Override
		public boolean jdbcCompliant() {
			return this.driver.jdbcCompliant();
		}

		@Override
		public Logger getParentLogger() throws SQLFeatureNotSupportedException {
			return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		}
	}
}
