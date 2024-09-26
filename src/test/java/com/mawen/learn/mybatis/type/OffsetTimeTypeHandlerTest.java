package com.mawen.learn.mybatis.type;

import java.time.OffsetTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OffsetTimeTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<OffsetTime> TYPE_HANDLER = new OffsetTimeTypeHandler();

	private static final OffsetTime OFFSET_TIME = OffsetTime.now();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, OFFSET_TIME, null);
		verify(ps).setObject(1, OFFSET_TIME);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getObject("column", OffsetTime.class)).thenReturn(OFFSET_TIME);
		assertEquals(OFFSET_TIME, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getObject("column", OffsetTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getObject(1, OffsetTime.class)).thenReturn(OFFSET_TIME);
		assertEquals(OFFSET_TIME, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getObject(1, OffsetTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getObject(1, OffsetTime.class)).thenReturn(OFFSET_TIME);
		assertEquals(OFFSET_TIME, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getObject(1, OffsetTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}
}