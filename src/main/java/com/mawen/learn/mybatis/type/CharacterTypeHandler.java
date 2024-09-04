package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class CharacterTypeHandler extends BaseTypeHandler<Character> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Character parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.toString());
	}

	@Override
	public Character getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return toCharacter(rs.getString(columnName));
	}

	@Override
	public Character getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return toCharacter(rs.getString(columnIndex));
	}

	@Override
	public Character getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return toCharacter(cs.getString(columnIndex));
	}

	private Character toCharacter(String value) {
		return value == null || value.isEmpty() ? null : value.charAt(0);
	}
}
