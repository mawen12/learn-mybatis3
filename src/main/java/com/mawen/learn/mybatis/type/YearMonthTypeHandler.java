package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class YearMonthTypeHandler extends BaseTypeHandler<YearMonth> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, YearMonth parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.toString());
	}

	@Override
	public YearMonth getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toYearMonth(rs.getString(columnName));
	}

	@Override
	public YearMonth getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toYearMonth(rs.getString(columnIndex));
	}

	@Override
	public YearMonth getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toYearMonth(cs.getString(columnIndex));
	}

	private YearMonth toYearMonth(String value) {
		return value == null ? null : YearMonth.parse(value);
	}
}
