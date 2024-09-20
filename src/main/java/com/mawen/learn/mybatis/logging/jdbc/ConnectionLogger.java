package com.mawen.learn.mybatis.logging.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.reflection.ExceptionUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class ConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

	private final Connection connection;

	private ConnectionLogger(Connection conn, Log statementLog, int queryStack) {
		super(statementLog, queryStack);
		this.connection = conn;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}

			if ("prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName())) {
				if (isDebugEnabled()) {
					debug(" Preparing: " + removeExtraWithWhitespace((String) args[0]), true);
				}
				PreparedStatement stmt = (PreparedStatement) method.invoke(connection, args);
				stmt = PreparedStatementLogger.newInstance(stmt, statementLog, queryStack);
				return stmt;
			}
			else if ("createStatement".equals(method.getName())) {
				Statement stmt = (Statement) method.invoke(connection, args);
				stmt = StatementLogger.newInstance(stmt, statementLog, queryStack);
				return stmt;
			}
			else {
				return method.invoke(connection, args);
			}
		}
		catch (Throwable t) {
			throw ExceptionUtil.unwrapThrowable(t);
		}
	}

	public static Connection newInstance(Connection conn, Log statementLog, int queryStack) {
		ConnectionLogger handler = new ConnectionLogger(conn, statementLog, queryStack);
		ClassLoader cl = Connection.class.getClassLoader();
		return (Connection) Proxy.newProxyInstance(cl, new Class[] {Connection.class}, handler);
	}

	public Connection getConnection() {
		return connection;
	}
}
