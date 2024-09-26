package com.mawen.learn.mybatis.type;

import com.mawen.learn.mybatis.session.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnknownTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Object> TYPE_HANDLER = spy(new UnknownTypeHandler(new Configuration()));

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
		verify(ps).setString(1, "Hello");
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(1);
		when(rsmd.getColumnLabel(1)).thenReturn("column");
		when(rsmd.getColumnClassName(1)).thenReturn(String.class.getName());
		when(rsmd.getColumnType(1)).thenReturn(JdbcType.VARCHAR.TYPE_CODE);

		when(rs.getString("column")).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(1);
		when(rsmd.getColumnLabel(1)).thenReturn("column");
		when(rsmd.getColumnClassName(1)).thenReturn(String.class.getName());
		when(rsmd.getColumnType(1)).thenReturn(JdbcType.VARCHAR.TYPE_CODE);

		when(rs.getString("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnClassName(1)).thenReturn(String.class.getName());
		when(rsmd.getColumnType(1)).thenReturn(JdbcType.VARCHAR.TYPE_CODE);

		when(rs.getString(1)).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnClassName(1)).thenReturn(String.class.getName());
		when(rsmd.getColumnType(1)).thenReturn(JdbcType.VARCHAR.TYPE_CODE);

		when(rs.getString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getObject(1)).thenReturn("Hello");
		assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getObject(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}