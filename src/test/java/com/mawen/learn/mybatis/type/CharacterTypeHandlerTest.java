package com.mawen.learn.mybatis.type;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CharacterTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Character> TYPE_HANDLER = new CharacterTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, 'a', null);
		verify(ps).setString(1, "a");
	}

	@Test
	public void shouldSetNullParameter() throws SQLException {
		TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.VARCHAR);
		verify(ps).setNull(1, JdbcType.VARCHAR.TYPE_CODE);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getString("column")).thenReturn("a");
		assertEquals(Character.valueOf('a'), TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getString("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getString(1)).thenReturn("a");
		assertEquals(Character.valueOf('a'), TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getString(1)).thenReturn("a");
		assertEquals(Character.valueOf('a'), TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}
}