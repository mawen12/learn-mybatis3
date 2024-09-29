package com.mawen.learn.mybatis.type;

import java.time.LocalTime;

import org.assertj.core.util.diff.Delta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalTimeTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<LocalTime> TYPE_HANDLER = new LocalTimeTypeHandler();

	private static final LocalTime LOCAL_TIME = LocalTime.now();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, LOCAL_TIME, null);
		verify(ps).setObject(1, LOCAL_TIME);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getObject("column", LocalTime.class)).thenReturn(LOCAL_TIME);
		assertEquals(LOCAL_TIME, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getObject("column", LocalTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getObject(1, LocalTime.class)).thenReturn(LOCAL_TIME);
		assertEquals(LOCAL_TIME, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getObject(1, LocalTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getObject(1, LocalTime.class)).thenReturn(LOCAL_TIME);
		assertEquals(LOCAL_TIME, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getObject(1, LocalTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}
}