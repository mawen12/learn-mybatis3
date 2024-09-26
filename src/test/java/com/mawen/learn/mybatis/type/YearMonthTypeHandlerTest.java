package com.mawen.learn.mybatis.type;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YearMonthTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<YearMonth> TYPE_HANDLER = new YearMonthTypeHandler();

	private static final YearMonth YEAR_MONTH = YearMonth.now();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, YEAR_MONTH, null);
		verify(ps).setString(1, YEAR_MONTH.toString());
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getString("column")).thenReturn(YEAR_MONTH.toString());
		assertEquals(YEAR_MONTH, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getString("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getString(1)).thenReturn(YEAR_MONTH.toString());
		assertEquals(YEAR_MONTH, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getString(1)).thenReturn(YEAR_MONTH.toString());
		assertEquals(YEAR_MONTH, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}
}