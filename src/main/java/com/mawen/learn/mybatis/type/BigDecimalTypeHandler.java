package com.mawen.learn.mybatis.type;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class BigDecimalTypeHandler extends BaseTypeHandler<BigDecimal> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, BigDecimal parameter, JdbcType jdbcType) throws SQLException {
		ps.setBigDecimal(i, parameter);
	}

	@Override
	public BigDecimal getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return rs.getBigDecimal(columnName);
	}

	@Override
	public BigDecimal getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getBigDecimal(columnIndex);
	}
}
