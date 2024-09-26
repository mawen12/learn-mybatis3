package com.mawen.learn.mybatis.type;

import java.time.Year;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YearTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Year> TYPE_HANDLER = new YearTypeHandler();

	private static final Year YEAR = Year.now();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, YEAR, null);
		verify(ps).setInt(1, YEAR.getValue());
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getInt("column")).thenReturn(YEAR.getValue(), 0);
		assertEquals(YEAR, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals(Year.of(0), TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getInt("column")).thenReturn(0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getInt(1)).thenReturn(YEAR.getValue(), 0);
		assertEquals(YEAR, TYPE_HANDLER.getResult(rs, 1));
		assertEquals(Year.of(0), TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getInt(1)).thenReturn(0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getInt(1)).thenReturn(YEAR.getValue(), 0);
		assertEquals(YEAR, TYPE_HANDLER.getResult(cs, 1));
		assertEquals(Year.of(0), TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getInt(1)).thenReturn(0);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}