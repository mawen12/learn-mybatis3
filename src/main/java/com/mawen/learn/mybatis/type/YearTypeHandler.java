package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class YearTypeHandler extends BaseTypeHandler<Year> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Year parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i, parameter.getValue());
	}

	@Override
	public Year getNullableResult(ResultSet rs, String columnName) throws SQLException {
		int year = rs.getInt(columnName);
		return year == 0 && rs.wasNull() ? null : Year.of(year);
	}

	@Override
	public Year getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		int year = rs.getInt(columnIndex);
		return year == 0 && rs.wasNull() ? null : Year.of(year);
	}

	@Override
	public Year getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		int year = cs.getInt(columnIndex);
		return year == 0 && cs.wasNull() ? null : Year.of(year);
	}
}
