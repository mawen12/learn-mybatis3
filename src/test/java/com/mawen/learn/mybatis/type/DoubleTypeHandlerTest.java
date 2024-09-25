package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoubleTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Double> TYPE_HANDLER = new DoubleTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, 100d, null);
		verify(ps).setDouble(1, 100d);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getDouble("column")).thenReturn(100d, 0d);
		assertEquals(100d, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals(0d, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getDouble("column")).thenReturn(0d);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getDouble(1)).thenReturn(100d, 0d);
		assertEquals(100d, TYPE_HANDLER.getResult(rs, 1));
		assertEquals(0d, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getDouble(1)).thenReturn(0d);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getDouble(1)).thenReturn(100d, 0d);
		assertEquals(100d, TYPE_HANDLER.getResult(cs, 1));
		assertEquals(0d, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getDouble(1)).thenReturn(0d);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}