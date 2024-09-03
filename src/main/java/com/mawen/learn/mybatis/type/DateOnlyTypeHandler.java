package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DateOnlyTypeHandler extends BaseTypeHandler<Date> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
		ps.setDate(i, new java.sql.Date(parameter.getTime()));
	}

	@Override
	public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toSqlDate(rs.getDate(columnName));
	}

	@Override
	public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toSqlDate(rs.getDate(columnIndex));
	}

	@Override
	public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toSqlDate(cs.getDate(columnIndex));
	}

	private java.sql.Date toSqlDate(Date date) {
		return date == null ? null : new java.sql.Date(date.getTime());
	}
}
