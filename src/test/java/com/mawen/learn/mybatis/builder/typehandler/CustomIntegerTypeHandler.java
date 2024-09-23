package com.mawen.learn.mybatis.builder.typehandler;

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
@MappedTypes(Integer.class)
public class CustomIntegerTypeHandler implements TypeHandler<Integer> {

	@Override
	public void setParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i,parameter);
	}

	@Override
	public Integer getResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getInt(columnName);
	}

	@Override
	public Integer getResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getInt(columnIndex);
	}

	@Override
	public Integer getResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getInt(columnIndex);
	}
}
