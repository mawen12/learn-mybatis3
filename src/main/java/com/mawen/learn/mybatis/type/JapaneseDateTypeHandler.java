package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.JapaneseDate;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class JapaneseDateTypeHandler extends BaseTypeHandler<JapaneseDate> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, JapaneseDate parameter, JdbcType jdbcType) throws SQLException {
		ps.setDate(i, Date.valueOf(LocalDate.ofEpochDay(parameter.toEpochDay())));
	}

	@Override
	public JapaneseDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return getJapaneseDate(rs.getDate(columnName));
	}

	@Override
	public JapaneseDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return getJapaneseDate(rs.getDate(columnIndex));
	}

	@Override
	public JapaneseDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return getJapaneseDate(cs.getDate(columnIndex));
	}

	private static JapaneseDate getJapaneseDate(Date date) {
		return date == null ? null : JapaneseDate.from(date.toLocalDate());
	}
}
