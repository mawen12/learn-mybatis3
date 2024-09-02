package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public interface TypeHandler<T> {

	void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

	T getResult(ResultSet rs, String columnName) throws SQLException;

	T getResult(ResultSet rs, int columnIndex) throws SQLException;

	T getResult(CallableStatement cs, int columnIndex) throws SQLException;
}
