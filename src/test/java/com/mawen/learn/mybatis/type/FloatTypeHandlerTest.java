package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FloatTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<Float> TYPE_HANDLER = new FloatTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, 100f, null);
		verify(ps).setFloat(1, 100f);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getFloat("column")).thenReturn(100f, 0f);
		assertEquals(100f, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals(0f, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getFloat("column")).thenReturn(0f);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getFloat(1)).thenReturn(100f, 0f);
		assertEquals(100f, TYPE_HANDLER.getResult(rs, 1));
		assertEquals(0f, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getFloat(1)).thenReturn(0f);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getFloat(1)).thenReturn(100f, 0f);
		assertEquals(100f, TYPE_HANDLER.getResult(cs, 1));
		assertEquals(0f, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getFloat(1)).thenReturn(0f);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}