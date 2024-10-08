package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class ObjectTypeHandler extends BaseTypeHandler<Object> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter);
	}

	@Override
	public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getObject(columnName);
	}

	@Override
	public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getObject(columnIndex);
	}

	@Override
	public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getObject(columnIndex);
	}
}
