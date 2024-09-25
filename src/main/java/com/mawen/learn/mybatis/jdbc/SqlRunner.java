package com.mawen.learn.mybatis.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
public class SqlRunner {

	public static final int NO_GENERATED_KEY = Integer.MIN_VALUE + 10001;

	private final Connection connection;
	private final TypeHandlerRegistry typeHandlerRegistry;
	private boolean useGeneratedKeySupport;

	public SqlRunner(Connection connection) {
		this.connection = connection;
		this.typeHandlerRegistry = new TypeHandlerRegistry();
	}

	public void setUseGeneratedKeySupport(boolean useGeneratedKeySupport) {
		this.useGeneratedKeySupport = useGeneratedKeySupport;
	}

	public Map<String, Object> selectOne(String sql, Object... args) throws SQLException {
		List<Map<String, Object>> results = selectAll(sql, args);
		if (results.size() != 1) {
			throw new SQLException("Statement returned " + results.size() + " results where exactly one (1) was expected.");
		}
		return results.get(0);
	}

	public List<Map<String, Object>> selectAll(String sql, Object... args) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			setParameters(ps, args);
			try (ResultSet rs = ps.executeQuery()) {
				return getResults(rs);
			}
		}
	}

	public int insert(String sql, Object... args) throws SQLException {
		PreparedStatement ps;
		if (useGeneratedKeySupport) {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		}
		else {
			ps = connection.prepareStatement(sql);
		}

		try {
			setParameters(ps, args);
			ps.executeUpdate();

			if (useGeneratedKeySupport) {
				try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
					List<Map<String, Object>> keys = getResults(generatedKeys);
					if (keys.size() == 1) {
						Map<String, Object> key = keys.get(0);
						Iterator<Object> i = key.values().iterator();
						if (i.hasNext()) {
							Object genKey = i.next();
							if (genKey != null) {
								try {
									return Integer.parseInt(genKey.toString());
								}
								catch (NumberFormatException ignored) {
								}
							}
						}
					}
				}
			}

			return NO_GENERATED_KEY;
		}
		finally {
			try {
				ps.close();
			}
			catch (SQLException ignored) {

			}
		}
	}

	public int delete(String sql, Object... args) throws SQLException {
		return update(sql, args);
	}

	public int update(String sql, Object... args) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			setParameters(ps, args);
			return ps.executeUpdate();
		}
	}

	public void run(String sql) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(sql);
		}
	}

	public void closeConnection() {
		try {
			connection.close();
		}
		catch (SQLException ignored) {

		}
	}

	private void setParameters(PreparedStatement ps, Object... args) throws SQLException {
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				throw new SQLException("SqlRunner requires an instance of NUll to represent typed null values for JDBC compatibility");
			}
			else if (args[i] instanceof Null) {
				((Null) args[i]).getTypeHandler().setParameter(ps, i + 1, null, ((Null) args[i]).getJdbcType());
			}
			else {
				TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(args[i].getClass());
				if (typeHandler == null) {
					throw new SQLException("SqlRunner could not find a TypeHandler instance for " + args[i].getClass());
				}
				else {
					typeHandler.setParameter(ps, i + 1, args[i], null);
				}
			}
		}
	}

	private List<Map<String, Object>> getResults(ResultSet rs) throws SQLException {
		List<Map<String, Object>> list = new ArrayList<>();
		List<String> columns = new ArrayList<>();
		List<TypeHandler<?>> typeHandlers = new ArrayList<>();
		ResultSetMetaData rsmd = rs.getMetaData();

		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			columns.add(rsmd.getColumnLabel(i + 1));

			try {
				Class<?> type = Resources.classForName(rsmd.getColumnClassName(i + 1));
				TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(type);
				if (typeHandler == null) {
					typeHandler = typeHandlerRegistry.getTypeHandler(Object.class);
				}
				typeHandlers.add(typeHandler);
			}
			catch (Exception e) {
				typeHandlers.add(typeHandlerRegistry.getTypeHandler(Object.class));
			}
		}

		while (rs.next()) {
			Map<String, Object> row = new HashMap<>();
			for (int i = 0; i < columns.size(); i++) {
				String name = columns.get(i);
				TypeHandler<?> typeHandler = typeHandlers.get(i);
				row.put(name.toUpperCase(Locale.ENGLISH), typeHandler.getResult(rs, name));
			}
			list.add(row);
		}

		return list;
	}
}
