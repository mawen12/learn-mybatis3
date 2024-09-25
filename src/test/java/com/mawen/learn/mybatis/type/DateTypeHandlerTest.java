package com.mawen.learn.mybatis.type;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DateTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Date> TYPE_HANDLER = new DateTypeHandler();

	private static final Date DATE = new Date();
	private static final Timestamp TIMESTAMP = new Timestamp(DATE.getTime());

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, DATE, null);
		verify(ps).setTimestamp(1, TIMESTAMP);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getTimestamp("column")).thenReturn(TIMESTAMP);
		assertEquals(DATE, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getTimestamp("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getTimestamp(1)).thenReturn(TIMESTAMP);
		assertEquals(DATE, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getTimestamp(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getTimestamp(1)).thenReturn(TIMESTAMP);
		assertEquals(DATE, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getTimestamp(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(rs, never()).wasNull();
	}
}