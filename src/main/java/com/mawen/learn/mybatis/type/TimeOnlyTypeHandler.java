package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class TimeOnlyTypeHandler extends BaseTypeHandler<Date> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
		ps.setTime(i, new Time(parameter.getTime()));
	}

	@Override
	public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toDate(rs.getTime(columnName));
	}

	@Override
	public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toDate(rs.getTime(columnIndex));
	}

	@Override
	public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toDate(cs.getTime(columnIndex));
	}

	private Date toDate(Time time) {
		return time == null ? null : new Date(time.getTime());
	}
}
