package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class InstantTypeHandler extends BaseTypeHandler<Instant> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType) throws SQLException {
		ps.setTimestamp(i, Timestamp.from(parameter));
	}

	@Override
	public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toInstant(rs.getTimestamp(columnName));
	}

	@Override
	public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toInstant(rs.getTimestamp(columnIndex));
	}

	@Override
	public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toInstant(cs.getTimestamp(columnIndex));
	}

	private Instant toInstant(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toInstant();
	}
}
