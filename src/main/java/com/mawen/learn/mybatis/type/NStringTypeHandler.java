package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class NStringTypeHandler extends BaseTypeHandler<String> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
		ps.setNString(i, parameter);
	}

	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getNString(columnName);
	}

	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getNString(columnIndex);
	}

	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getNString(columnIndex);
	}
}
