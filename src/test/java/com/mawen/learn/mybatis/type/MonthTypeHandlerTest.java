package com.mawen.learn.mybatis.type;

import java.time.Month;

import com.mawen.learn.mybatis.executor.result.ResultMapException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonthTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Month> TYPE_HANDLER = new MonthTypeHandler();

	private static final Month MONTH = Month.JANUARY;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, MONTH, null);
		verify(ps).setInt(1, MONTH.getValue());
	}
	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getInt("column")).thenReturn(MONTH.getValue(), 0);
		assertEquals(MONTH, TYPE_HANDLER.getResult(rs, "column"));
		assertThrows(ResultMapException.class, () -> {
					TYPE_HANDLER.getResult(rs, "column");
				},
				"Error attempting to get column 'column' from result set.  Cause: java.time.DateTimeException: Invalid value for MonthOfYear: 0");
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
		when(rs.getInt(1)).thenReturn(MONTH.getValue(), 0);
		assertEquals(MONTH, TYPE_HANDLER.getResult(rs, 1));
		assertThrows(ResultMapException.class, () -> {
					TYPE_HANDLER.getResult(rs, 1);
				},
				"Error attempting to get column 'column' from result set.  Cause: java.time.DateTimeException: Invalid value for MonthOfYear: 0");
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
		when(cs.getInt(1)).thenReturn(MONTH.getValue(), 0);
		assertEquals(MONTH, TYPE_HANDLER.getResult(cs, 1));
		assertThrows(ResultMapException.class, () -> {
					TYPE_HANDLER.getResult(cs, 1);
				},
				"Error attempting to get column 'column' from result set.  Cause: java.time.DateTimeException: Invalid value for MonthOfYear: 0");
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getInt(1)).thenReturn(0);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}