package com.mawen.learn.mybatis.type;

import java.io.Reader;
import java.sql.Clob;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClobTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<String> TYPE_HANDLER = new ClobTypeHandler();

	@Mock
	protected Clob clob;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
		verify(ps).setCharacterStream(eq(1), any(Reader.class), eq(5));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getClob("column")).thenReturn(clob);
		when(clob.length()).thenReturn(3L);
		when(clob.getSubString(1, 3)).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getClob("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getClob(1)).thenReturn(clob);
		when(clob.length()).thenReturn(3L);
		when(clob.getSubString(1, 3)).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getClob(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getClob(1)).thenReturn(clob);
		when(clob.length()).thenReturn(3L);
		when(clob.getSubString(1, 3)).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getClob(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}