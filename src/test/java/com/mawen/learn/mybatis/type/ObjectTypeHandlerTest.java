package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObjectTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<Object> TYPE_HANDLER = new ObjectTypeHandler();

	private static final Object OBJECT = new Object();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, OBJECT, null);
		verify(ps).setObject(1, OBJECT);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getObject("column")).thenReturn(OBJECT);
		assertEquals(OBJECT, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getObject("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getObject(1)).thenReturn(OBJECT);
		assertEquals(OBJECT, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getObject(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getObject(1)).thenReturn(OBJECT);
		assertEquals(OBJECT, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getObject(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}