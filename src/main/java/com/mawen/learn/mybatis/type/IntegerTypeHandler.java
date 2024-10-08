package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i, parameter);
	}

	@Override
	public Integer getNullableResult(ResultSet rs, String columnName) throws SQLException {
		int result = rs.getInt(columnName);
		return result == 0 && rs.wasNull() ? null : result;
	}

	@Override
	public Integer getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		int result = rs.getInt(columnIndex);
		return result == 0 && rs.wasNull() ? null : result;
	}

	@Override
	public Integer getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		int result = cs.getInt(columnIndex);
		return result == 0 && cs.wasNull() ? null : result;
	}
}
