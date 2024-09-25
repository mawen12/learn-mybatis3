package com.mawen.learn.mybatis.type;

import com.mawen.learn.mybatis.BaseDataTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LongTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Long> TYPE_HANDLER = new LongTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, 100L, null);
		verify(ps).setLong(1, 100L);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getLong("column")).thenReturn(100L, 0L);
		assertEquals(100L, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals(0L, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getLong("column")).thenReturn(0L);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getLong(1)).thenReturn(100L, 0L);
		assertEquals(100L, TYPE_HANDLER.getResult(rs, 1));
		assertEquals(0L, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getLong(1)).thenReturn(0L);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getLong(1)).thenReturn(100L, 0L);
		assertEquals(100L, TYPE_HANDLER.getResult(cs, 1));
		assertEquals(0L, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getLong(1)).thenReturn(0L);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}