package com.mawen.learn.mybatis.type;

import java.sql.Connection;
import java.sql.SQLXML;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqlxmlTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<String> TYPE_HANDLER = new SqlxmlTypeHandler();

	private static final String XML_STR = "<message>test</message>";

	@Mock
	private Connection connection;

	@Mock
	private SQLXML sqlxml;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		when(ps.getConnection()).thenReturn(connection);
		when(connection.createSQLXML()).thenReturn(sqlxml);

		TYPE_HANDLER.setParameter(ps, 1, XML_STR, null);
		verify(ps).setSQLXML(1, sqlxml);
		verify(sqlxml).setString(XML_STR);
		verify(sqlxml).free();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getSQLXML("column")).thenReturn(sqlxml);
		when(sqlxml.toString()).thenReturn(XML_STR);
		assertEquals(XML_STR, TYPE_HANDLER.getResult(rs, "column"));
		verify(sqlxml).free();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getSQLXML("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getSQLXML(1)).thenReturn(sqlxml);
		when(sqlxml.toString()).thenReturn(XML_STR);
		assertEquals(XML_STR, TYPE_HANDLER.getResult(rs, 1));
		verify(sqlxml).free();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getSQLXML(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getSQLXML(1)).thenReturn(sqlxml);
		when(sqlxml.toString()).thenReturn(XML_STR);
		assertEquals(XML_STR, TYPE_HANDLER.getResult(cs, 1));
		verify(sqlxml).free();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getSQLXML(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}