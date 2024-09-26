package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Short> TYPE_HANDLER = new ShortTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, (short) 100, null);
		verify(ps).setShort(1, (short) 100);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getShort("column")).thenReturn((short) 100, (short) 0);
		assertEquals((short) 100, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals((short) 0, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getShort("column")).thenReturn((short) 0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getShort(1)).thenReturn((short) 100, (short) 0);
		assertEquals((short) 100, TYPE_HANDLER.getResult(rs, 1));
		assertEquals((short) 0, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getShort(1)).thenReturn((short) 0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getShort(1)).thenReturn((short) 100, (short) 0);
		assertEquals((short) 100, TYPE_HANDLER.getResult(cs, 1));
		assertEquals((short) 0, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getShort(1)).thenReturn((short) 0);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs).wasNull();
	}
}