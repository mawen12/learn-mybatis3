package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DateTypeHandler extends BaseTypeHandler<Date> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
		ps.setTimestamp(i, new Timestamp(parameter.getTime()));
	}

	@Override
	public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toDate(rs.getTimestamp(columnName));
	}

	@Override
	public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toDate(rs.getTimestamp(columnIndex));
	}

	@Override
	public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toDate(cs.getTimestamp(columnIndex));
	}

	private Date toDate(Timestamp timestamp) {
		return timestamp == null ? null : new Date(timestamp.getTime());
	}
}
