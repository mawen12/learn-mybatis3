package com.mawen.learn.mybatis.type;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OffsetDateTimeTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<OffsetDateTime> TYPE_HANDLER = new OffsetDateTimeTypeHandler();

	private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, OFFSET_DATE_TIME, null);
		verify(ps).setObject(1, OFFSET_DATE_TIME);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getObject("column", OffsetDateTime.class)).thenReturn(OFFSET_DATE_TIME);
		assertEquals(OFFSET_DATE_TIME, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getObject("column", OffsetDateTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getObject(1, OffsetDateTime.class)).thenReturn(OFFSET_DATE_TIME);
		assertEquals(OFFSET_DATE_TIME, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getObject(1, OffsetDateTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getObject(1, OffsetDateTime.class)).thenReturn(OFFSET_DATE_TIME);
		assertEquals(OFFSET_DATE_TIME, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getObject(1, OffsetDateTime.class)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}
}