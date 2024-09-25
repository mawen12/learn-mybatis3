package com.mawen.learn.mybatis.type;


import java.sql.Array;
import java.sql.Connection;
import java.sql.Types;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArrayTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Object> TYPE_HANDLER = new ArrayTypeHandler();

	@Mock
	Array mockArray;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, mockArray, null);
		verify(ps).setArray(1, mockArray);
	}

	@Test
	public void shouldSetStringArrayParameter() throws Exception {
		Connection connection = mock(Connection.class);
		when(ps.getConnection()).thenReturn(connection);

		Array array = mock(Array.class);
		when(connection.createArrayOf(anyString(), any(String[].class))).thenReturn(array);

		TYPE_HANDLER.setParameter(ps, 1, new String[]{"Hello World"}, JdbcType.ARRAY);
		verify(ps).setArray(1, array);
		verify(array).free();
	}

	@Test
	public void shouldSetNullParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.ARRAY);
		verify(ps).setNull(1, Types.ARRAY);
	}

	@Test
	public void shouldFailForNonArrayParameter() {
		assertThrows(TypeException.class, () -> {
			TYPE_HANDLER.setParameter(ps, 1, "unsupported parameter type", null);
		});
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		String[] stringArray = {"a", "b"};
		when(rs.getArray("column")).thenReturn(mockArray);
		when(mockArray.getArray()).thenReturn(stringArray);

		assertEquals(stringArray, TYPE_HANDLER.getResult(rs, "column"));
		verify(mockArray).free();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getArray("column")).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		String[] stringArray = {"a", "b"};
		when(rs.getArray(1)).thenReturn(mockArray);
		when(mockArray.getArray()).thenReturn(stringArray);

		assertEquals(stringArray, TYPE_HANDLER.getResult(rs, 1));
		verify(mockArray).free();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getArray(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		String[] stringArray = {"a", "b"};
		when(cs.getArray(1)).thenReturn(mockArray);
		when(mockArray.getArray()).thenReturn(stringArray);

		assertEquals(stringArray, TYPE_HANDLER.getResult(cs, 1));
		verify(mockArray).free();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getArray(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}