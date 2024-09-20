package com.mawen.learn.mybatis.logging.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.reflection.ExceptionUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class StatementLogger extends BaseJdbcLogger implements InvocationHandler {

	private final Statement statement;

	private StatementLogger(Statement stmt, Log statementLog, int queryStack) {
		super(statementLog, queryStack);
		this.statement = stmt;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}

			if (EXECUTE_METHODS.contains(method.getName())) {
				if (isDebugEnabled()) {
					debug(" Executing: " + removeExtraWithWhitespace((String) args[0]), true);
				}
				if ("executeQuery".equals(method.getName())) {
					ResultSet rs = (ResultSet) method.invoke(statement, args);
					return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
				}
				else {
					return method.invoke(statement, args);
				}
			}
			else if ("getResultSet".equals(method.getName())) {
				ResultSet rs = (ResultSet) method.invoke(statement, args);
				return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
			}
			else {
				return method.invoke(statement, args);
			}
		}
		catch (Throwable t) {
			throw ExceptionUtil.unwrapThrowable(t);
		}
	}

	public static Statement newInstance(Statement stmt, Log statementLog, int queryStack) {
		StatementLogger handler = new StatementLogger(stmt, statementLog, queryStack);
		ClassLoader cl = Statement.class.getClassLoader();
		return (Statement) Proxy.newProxyInstance(cl, new Class[] {Statement.class}, handler);
	}

	public Statement getStatement() {
		return statement;
	}
}
