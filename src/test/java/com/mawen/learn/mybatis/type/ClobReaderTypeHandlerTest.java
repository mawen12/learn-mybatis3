package com.mawen.learn.mybatis.type;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClobReaderTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Reader> TYPE_HANDLER = new ClobReaderTypeHandler();

	@Mock
	protected Clob clob;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		Reader reader = new StringReader("Hello");
		TYPE_HANDLER.setParameter(ps, 1, reader, null);
		verify(ps).setClob(1, reader);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		Reader reader = new StringReader("Hello");
		when(rs.getClob("column")).thenReturn(clob);
		when(clob.getCharacterStream()).thenReturn(reader);
		assertEquals(reader, TYPE_HANDLER.getResult(rs, "column"));
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
		Reader reader = new StringReader("Hello");
		when(rs.getClob(1)).thenReturn(clob);
		when(clob.getCharacterStream()).thenReturn(reader);
		assertEquals(reader, TYPE_HANDLER.getResult(rs, 1));
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
		Reader reader = new StringReader("Hello");
		when(cs.getClob(1)).thenReturn(clob);
		when(clob.getCharacterStream()).thenReturn(reader);
		assertEquals(reader, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getClob(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}