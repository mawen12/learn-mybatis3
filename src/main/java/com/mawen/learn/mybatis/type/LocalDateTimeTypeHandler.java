package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter);
	}

	@Override
	public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getObject(columnName, LocalDateTime.class);
	}

	@Override
	public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getObject(columnIndex, LocalDateTime.class);
	}

	@Override
	public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getObject(columnIndex, LocalDateTime.class);
	}
}
