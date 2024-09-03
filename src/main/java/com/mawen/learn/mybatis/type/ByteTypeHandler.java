package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ByteTypeHandler extends BaseTypeHandler<Byte> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Byte parameter, JdbcType jdbcType) throws SQLException {
		ps.setByte(i, parameter);
	}

	@Override
	public Byte getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getByte(columnName);
	}

	@Override
	public Byte getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getByte(columnIndex);
	}

	@Override
	public Byte getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getByte(columnIndex);
	}
}
