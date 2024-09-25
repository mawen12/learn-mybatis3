package com.mawen.learn.mybatis.type;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BigIntegerTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<BigInteger> TYPE_HANDLER = new BigIntegerTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, new BigInteger("707070656505050302797979792923232303"), null);
		verify(ps).setBigDecimal(1, new BigDecimal("707070656505050302797979792923232303"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getBigDecimal("column")).thenReturn(new BigDecimal("707070656505050302797979792923232303"));

		assertEquals(new BigInteger("707070656505050302797979792923232303"), TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getBigDecimal("column")).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getBigDecimal(1)).thenReturn(new BigDecimal("707070656505050302797979792923232303"));

		assertEquals(new BigInteger("707070656505050302797979792923232303"), TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getBigDecimal(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getBigDecimal(1)).thenReturn(new BigDecimal("707070656505050302797979792923232303"));

		assertEquals(new BigInteger("707070656505050302797979792923232303"), TYPE_HANDLER.getResult(cs, 1));
	}

	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getBigDecimal(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}