package com.mawen.learn.mybatis.type;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqlTimestampTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Timestamp> TYPE_HANDLER = new SqlTimestampTypeHandler();

	private static final Timestamp SQL_TIME = new Timestamp(new Date().getTime());

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, SQL_TIME, null);
		verify(ps).setTimestamp(1, SQL_TIME);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getTimestamp("column")).thenReturn(SQL_TIME);
		assertEquals(SQL_TIME, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getTimestamp("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getTimestamp(1)).thenReturn(SQL_TIME);
		assertEquals(SQL_TIME, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getTimestamp(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getTimestamp(1)).thenReturn(SQL_TIME);
		assertEquals(SQL_TIME, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getTimestamp(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}