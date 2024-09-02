package com.mawen.learn.mybatis.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class BigIntegerTypeHandler extends BaseTypeHandler<BigInteger> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, BigInteger parameter, JdbcType jdbcType) throws SQLException {
		ps.setBigDecimal(i, new BigDecimal(parameter));
	}

	@Override
	public BigInteger getNullableResult(ResultSet rs, String columnName) throws SQLException {
		BigDecimal bigDecimal = rs.getBigDecimal(columnName);
		return bigDecimal == null ? null : bigDecimal.toBigInteger();
	}

	@Override
	public BigInteger getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		BigDecimal bigDecimal = rs.getBigDecimal(columnIndex);
		return bigDecimal == null ? null : bigDecimal.toBigInteger();
	}

	@Override
	public BigInteger getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		BigDecimal bigDecimal = cs.getBigDecimal(columnIndex);
		return bigDecimal == null ? null : bigDecimal.toBigInteger();
	}
}
