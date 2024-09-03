package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetTime;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class OffsetTimeTypeHandler extends BaseTypeHandler<OffsetTime> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, OffsetTime parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter);
	}

	@Override
	public OffsetTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getObject(columnName, OffsetTime.class);
	}

	@Override
	public OffsetTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getObject(columnIndex, OffsetTime.class);
	}

	@Override
	public OffsetTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getObject(columnIndex, OffsetTime.class);
	}
}
