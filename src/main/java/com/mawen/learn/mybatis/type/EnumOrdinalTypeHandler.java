package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class EnumOrdinalTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

	private final Class<E> type;
	private final E[] enums;

	public EnumOrdinalTypeHandler(Class<E> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}

		this.type = type;
		this.enums = type.getEnumConstants();
		if (this.enums == null) {
			throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
		}
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i, parameter.ordinal());
	}

	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
		int original = rs.getInt(columnName);
		if (original == 0 && rs.wasNull()) {
			return null;
		}
		return toOrdinalEnum(original);
	}

	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		int ordinal = rs.getInt(columnIndex);
		if (ordinal == 0 && rs.wasNull()) {
			return null;
		}
		return toOrdinalEnum(ordinal);
	}

	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		int ordinal = cs.getInt(columnIndex);
		if (ordinal == 0 && cs.wasNull()) {
			return null;
		}
		return toOrdinalEnum(ordinal);
	}

	private E toOrdinalEnum(int ordinal) {
		try {
			return enums[ordinal];
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Cannot convert " + ordinal + " to " + type.getSimpleName() + " by ordinal value.", e);
		}
	}
}
