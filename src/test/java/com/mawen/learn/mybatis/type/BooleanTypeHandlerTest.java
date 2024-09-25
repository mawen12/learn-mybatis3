package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BooleanTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Boolean> TYPE_HANDLER = new BooleanTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, true, null);
		verify(ps).setBoolean(1, true);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getBoolean("column")).thenReturn(true, false);
		assertEquals(true, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals(false, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getBoolean("column")).thenReturn(false);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getBoolean(1)).thenReturn(true, false);
		assertEquals(true, TYPE_HANDLER.getResult(rs, 1));
		assertEquals(false, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getBoolean(1)).thenReturn(false);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getBoolean(1)).thenReturn(true, false);
		assertEquals(true, TYPE_HANDLER.getResult(cs, 1));
		assertEquals(false, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getBoolean(1)).thenReturn(false);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}