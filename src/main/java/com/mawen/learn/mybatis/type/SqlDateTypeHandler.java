package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class SqlDateTypeHandler extends BaseTypeHandler<Date> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
		ps.setDate(i, parameter);
	}

	@Override
	public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getDate(columnName);
	}

	@Override
	public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getDate(columnIndex);
	}

	@Override
	public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getDate(columnIndex);
	}
}
