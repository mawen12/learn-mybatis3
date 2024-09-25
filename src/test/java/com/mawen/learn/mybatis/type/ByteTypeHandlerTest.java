package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ByteTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<Byte> TYPE_HANDLER = new ByteTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, (byte) 100, null);
		verify(ps).setByte(1, (byte) 100);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getByte("column")).thenReturn((byte) 100, (byte) 0);
		assertEquals((byte) 100, TYPE_HANDLER.getResult(rs, "column"));
		assertEquals((byte) 0, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getByte("column")).thenReturn((byte) 0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getByte(1)).thenReturn((byte) 100, (byte) 0);
		assertEquals((byte) 100, TYPE_HANDLER.getResult(rs, 1));
		assertEquals((byte) 0, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getByte(1)).thenReturn((byte) 0);
		when(rs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getByte(1)).thenReturn((byte) 100, (byte) 0);
		assertEquals((byte) 100, TYPE_HANDLER.getResult(cs, 1));
		assertEquals((byte) 0, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getByte(1)).thenReturn((byte) 0);
		when(cs.wasNull()).thenReturn(true);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}