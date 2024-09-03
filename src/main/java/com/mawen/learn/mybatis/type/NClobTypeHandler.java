package com.mawen.learn.mybatis.type;

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class NClobTypeHandler extends BaseTypeHandler<String> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
		StringReader reader = new StringReader(parameter);
		ps.setCharacterStream(i, reader, parameter.length());
	}

	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toString(rs.getClob(columnName));
	}

	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toString(rs.getClob(columnIndex));
	}

	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toString(cs.getClob(columnIndex));
	}

	private String toString(Clob clob) throws SQLException {
		return clob == null ? null : clob.getSubString(1, (int) clob.length());
	}
}
