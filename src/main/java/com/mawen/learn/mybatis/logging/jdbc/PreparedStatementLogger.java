package com.mawen.learn.mybatis.logging.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.reflection.ExceptionUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class PreparedStatementLogger extends BaseJdbcLogger implements InvocationHandler {

	private final PreparedStatement statement;

	private PreparedStatementLogger(PreparedStatement statement, Log statementLog, int queryStack) {
		super(statementLog, queryStack);
		this.statement = statement;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}

			if (EXECUTE_METHODS.contains(method.getName())) {
				if (isDebugEnabled()) {
					debug("Parameters: " + getParameterValueString(), true);
				}
				clearColumnInfo();

				if ("executeQuery".equals(method.getName())) {
					ResultSet rs = (ResultSet) method.invoke(statement, args);
					return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
				}
				else {
					return method.invoke(statement, args);
				}
			}
			else if (SET_METHODS.contains(method.getName())) {
				if ("setNull".equals(method.getName())) {
					setColumn(args[0], null);
				}
				else {
					setColumn(args[0], args[1]);
				}
				return method.invoke(statement, args);
			}
			else if ("getResultSet".equals(method.getName())) {
				ResultSet rs = (ResultSet) method.invoke(statement, args);
				return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
			}
			else if ("getUpdateCount".equals(method.getName())) {
				int updateCount = (Integer) method.invoke(statement, args);
				if (updateCount == -1) {
					debug("  Updates: " + statement, false);
				}
				return updateCount;
			}
			else {
				return method.invoke(statement, args);
			}
		}
		catch (Throwable t) {
			throw ExceptionUtil.unwrapThrowable(t);
		}
	}

	public static PreparedStatement newInstance(PreparedStatement ps, Log statementLog, int queryStack) {
		PreparedStatementLogger handler = new PreparedStatementLogger(ps, statementLog, queryStack);
		ClassLoader cl = PreparedStatement.class.getClassLoader();
		return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[] {PreparedStatement.class, CallableStatement.class}, handler);
	}

	public PreparedStatement getStatement() {
		return statement;
	}
}
