package com.mawen.learn.mybatis.logging.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.reflection.ExceptionUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class ResultSetLogger extends BaseJdbcLogger implements InvocationHandler {

	private static final Set<Integer> BLOB_BYTES = new HashSet<>();

	private boolean first = true;
	private int rows;
	private final ResultSet rs;
	private final Set<Integer> blobColumns = new HashSet<>();

	private ResultSetLogger(ResultSet rs, Log statementLog, int queryStack) {
		super(statementLog, queryStack);
		this.rs = rs;
	}

	static {
		BLOB_BYTES.add(Types.BINARY);
		BLOB_BYTES.add(Types.BLOB);
		BLOB_BYTES.add(Types.CLOB);
		BLOB_BYTES.add(Types.LONGNVARCHAR);
		BLOB_BYTES.add(Types.LONGVARBINARY);
		BLOB_BYTES.add(Types.LONGVARCHAR);
		BLOB_BYTES.add(Types.NCHAR);
		BLOB_BYTES.add(Types.VARBINARY);
	}



	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}

			Object o = method.invoke(rs, args);
			if ("next".equals(method.getName())) {
				if ((Boolean) o) {
					rows++;
					if (isTraceEnabled()) {
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						if (first) {
							first = false;
							printColumnHeaders(rsmd, columnCount);
						}
						else {
							printColumnValues(columnCount);
						}
					}
				}
				else {
					debug("     Total: " + rows, false);
				}
			}
			clearColumnInfo();
			return o;
		}
		catch (Throwable t) {
			throw ExceptionUtil.unwrapThrowable(t);
		}
	}

	private void printColumnHeaders(ResultSetMetaData rsmd, int columnCount) throws SQLException {
		StringJoiner row = new StringJoiner(", ", "   Columns:", "");
		for (int i = 1; i < columnCount; i++) {
			if (BLOB_BYTES.contains(rsmd.getColumnType(i))) {
				blobColumns.add(i);
			}
			row.add(rsmd.getColumnLabel(i));
		}
		trace(row.toString(), false);
	}

	private void printColumnValues(int columnCount) {
		StringJoiner row = new StringJoiner(", ", "      Row: ", "");
		for (int i = 1; i < columnCount; i++) {
			try {
				if (blobColumns.contains(i)) {
					row.add("<<BLOB>>");
				}
				else {
					row.add(rs.getString(i));
				}
			}
			catch (SQLException e) {
				row.add("<<Cannot Display>>");
			}
		}
		trace(row.toString(), false);
	}

	public static ResultSet newInstance(ResultSet rs, Log statementLog, int queryStack) {
		ResultSetLogger handler = new ResultSetLogger(rs, statementLog, queryStack);
		ClassLoader cl = ResultSet.class.getClassLoader();
		return (ResultSet) Proxy.newProxyInstance(cl, new Class[] {ResultSet.class}, handler);
	}

	public ResultSet getRs() {
		return rs;
	}
}
