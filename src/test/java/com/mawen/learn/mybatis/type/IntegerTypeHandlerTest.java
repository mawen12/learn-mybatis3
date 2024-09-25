package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IntegerTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<Integer> TYPE_HANDLER = new IntegerTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, 100, null);
		verify(ps).setInt(1, 100);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getInt("column")).thenReturn(100, 0);
		assertEquals(100, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals(0, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getInt("column")).thenReturn(0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getInt(1)).thenReturn(100, 0);
		assertEquals(100, TYPE_HANDLER.getResult(rs, 1));
		assertEquals(0, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getInt(1)).thenReturn(0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getInt(1)).thenReturn(100, 0);
		assertEquals(100, TYPE_HANDLER.getResult(cs, 1));
		assertEquals(0, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getInt(1)).thenReturn(0);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}