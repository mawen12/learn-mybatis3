package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class LocalTimeTypeHandler extends BaseTypeHandler<LocalTime> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, LocalTime parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter);
	}

	@Override
	public LocalTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getObject(columnName, LocalTime.class);
	}

	@Override
	public LocalTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getObject(columnIndex, LocalTime.class);
	}

	@Override
	public LocalTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getObject(columnIndex, LocalTime.class);
	}
}
