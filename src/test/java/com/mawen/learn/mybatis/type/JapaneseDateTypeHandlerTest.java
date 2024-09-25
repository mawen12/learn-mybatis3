package com.mawen.learn.mybatis.type;

import java.sql.Date;
import java.time.LocalDate;
import java.time.chrono.JapaneseDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JapaneseDateTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<JapaneseDate> TYPE_HANDLER = new JapaneseDateTypeHandler();

	private static final JapaneseDate JAPANESE_DATE = JapaneseDate.now();
	private static final Date DATE = Date.valueOf(LocalDate.ofEpochDay(JAPANESE_DATE.toEpochDay()));

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, JAPANESE_DATE, null);
		verify(ps).setDate(1, DATE);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getDate("column")).thenReturn(DATE);
		assertEquals(JAPANESE_DATE, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getDate("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getDate(1)).thenReturn(DATE);
		assertEquals(JAPANESE_DATE, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getDate(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getDate(1)).thenReturn(DATE);
		assertEquals(JAPANESE_DATE, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getDate(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}
}