package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NStringTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<String> TYPE_HANDLER = new NStringTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
		verify(ps).setNString(1, "Hello");
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getNString("column")).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getNString("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getNString(1)).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getNString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getNString(1)).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getNString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}