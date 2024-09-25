package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DoubleTypeHandler extends BaseTypeHandler<Double> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Double parameter, JdbcType jdbcType) throws SQLException {
		ps.setDouble(i, parameter);
	}

	@Override
	public Double getNullableResult(ResultSet rs, String columnName) throws SQLException {
		double result = rs.getDouble(columnName);
		return result == 0 && rs.wasNull() ? null : result;
	}

	@Override
	public Double getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		double result = rs.getDouble(columnIndex);
		return result == 0 && rs.wasNull() ? null : result;
	}

	@Override
	public Double getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		double result = cs.getDouble(columnIndex);
		return result == 0 && cs.wasNull() ? null : result;
	}
}
