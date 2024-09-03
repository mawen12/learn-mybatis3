package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class BooleanTypeHandler extends BaseTypeHandler<Boolean> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
		ps.setBoolean(i, parameter);
	}

	@Override
	public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getBoolean(columnName);
	}

	@Override
	public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getBoolean(columnIndex);
	}

	@Override
	public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getBoolean(columnIndex);
	}
}
