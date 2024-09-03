package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class OffsetDateTimeTypeHandler extends BaseTypeHandler<OffsetDateTime> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, OffsetDateTime parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter);
	}

	@Override
	public OffsetDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getObject(columnName, OffsetDateTime.class);
	}

	@Override
	public OffsetDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getObject(columnIndex, OffsetDateTime.class);
	}

	@Override
	public OffsetDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getObject(columnIndex, OffsetDateTime.class);
	}
}
