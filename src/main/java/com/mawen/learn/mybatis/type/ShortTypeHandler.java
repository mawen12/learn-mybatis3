package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ShortTypeHandler extends BaseTypeHandler<Short> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Short parameter, JdbcType jdbcType) throws SQLException {
		ps.setShort(i, parameter);
	}

	@Override
	public Short getNullableResult(ResultSet rs, String columnName) throws SQLException {
		short result = rs.getShort(columnName);
		return result == 0 && rs.wasNull() ? null : result;
	}

	@Override
	public Short getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		short result = rs.getShort(columnIndex);
		return result == 0 && rs.wasNull() ? null : result;
	}

	@Override
	public Short getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		short result = cs.getShort(columnIndex);
		return result == 0 && cs.wasNull() ? null : result;
	}
}
