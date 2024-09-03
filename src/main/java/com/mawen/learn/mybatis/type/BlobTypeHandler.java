package com.mawen.learn.mybatis.type;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class BlobTypeHandler extends BaseTypeHandler<byte[]> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType) throws SQLException {
		ByteArrayInputStream bis = new ByteArrayInputStream(parameter);
		ps.setBinaryStream(i, bis, parameter.length);
	}

	@Override
	public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toPrimitiveBytes(rs.getBlob(columnName));
	}

	@Override
	public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toPrimitiveBytes(rs.getBlob(columnIndex));
	}

	@Override
	public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toPrimitiveBytes(cs.getBlob(columnIndex));
	}

	private byte[] toPrimitiveBytes(Blob blob) throws SQLException {
		return blob == null ? null : blob.getBytes(1, (int) blob.length());
	}
}
