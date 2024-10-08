package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class MonthTypeHandler extends BaseTypeHandler<Month> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Month parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i, parameter.getValue());
	}

	@Override
	public Month getNullableResult(ResultSet rs, String columnName) throws SQLException {
		int month = rs.getInt(columnName);
		return month == 0 && rs.wasNull() ? null : Month.of(month);
	}

	@Override
	public Month getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		int month = rs.getInt(columnIndex);
		return month == 0 && rs.wasNull() ? null : Month.of(month);
	}

	@Override
	public Month getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		int month = cs.getInt(columnIndex);
		return month == 0 && cs.wasNull() ? null : Month.of(month);
	}

}
