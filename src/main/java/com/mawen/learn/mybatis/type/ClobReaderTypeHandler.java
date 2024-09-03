package com.mawen.learn.mybatis.type;

import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class ClobReaderTypeHandler extends BaseTypeHandler<Reader> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Reader parameter, JdbcType jdbcType) throws SQLException {
		ps.setClob(i, parameter);
	}

	@Override
	public Reader getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toReader(rs.getClob(columnName));
	}

	@Override
	public Reader getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toReader(rs.getClob(columnIndex));
	}

	@Override
	public Reader getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toReader(cs.getClob(columnIndex));
	}

	private Reader toReader(Clob clob) throws SQLException {
		return clob == null ? null : clob.getCharacterStream();
	}
}
