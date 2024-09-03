package com.mawen.learn.mybatis.type;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class BlobInputStreamTypeHandler extends BaseTypeHandler<InputStream> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, InputStream parameter, JdbcType jdbcType) throws SQLException {
		ps.setBlob(i, parameter);
	}

	@Override
	public InputStream getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toInputStream(rs.getBlob(columnName));
	}

	@Override
	public InputStream getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toInputStream(rs.getBlob(columnIndex));
	}

	@Override
	public InputStream getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toInputStream(cs.getBlob(columnIndex));
	}

	private InputStream toInputStream(Blob blob) throws SQLException {
		return blob == null ? null : blob.getBinaryStream();
	}
}
