package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jdk.internal.util.ByteArray;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ByteObjectArrayTypeHandler extends BaseTypeHandler<Byte[]> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Byte[] parameter, JdbcType jdbcType) throws SQLException {
		ps.setBytes(i, ByteArrayUtils.convertToPrimitiveArray(parameter));
	}

	@Override
	public Byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toBytes(rs.getBytes(columnName));
	}

	@Override
	public Byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toBytes(rs.getBytes(columnIndex));
	}

	@Override
	public Byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toBytes(cs.getBytes(columnIndex));
	}

	private Byte[] toBytes(byte[] bytes) {
		return bytes == null ? null : ByteArrayUtils.convertToObjectArray(bytes);
	}
}
