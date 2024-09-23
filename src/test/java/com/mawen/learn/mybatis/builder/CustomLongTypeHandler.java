package com.mawen.learn.mybatis.builder;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.MappedTypes;
import com.mawen.learn.mybatis.type.TypeHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
@MappedTypes(Long.class)
public class CustomLongTypeHandler implements TypeHandler<Long> {

	@Override
	public void setParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
		ps.setLong(i,parameter);
	}

	@Override
	public Long getResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getLong(columnName);
	}

	@Override
	public Long getResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getLong(columnIndex);
	}

	@Override
	public Long getResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getLong(columnIndex);
	}
}
