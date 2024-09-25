package com.mawen.learn.mybatis.type;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnumTypeHandlerTest extends BaseTypeHandlerTest{

	private static final TypeHandler<MyEnum> TYPE_HANDLER = new EnumTypeHandler<>(MyEnum.class);

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, MyEnum.ONE, null);
		verify(ps).setString(1, "ONE");
	}

	@Test
	public void shouldSetNullParameter() throws SQLException {
		TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.VARCHAR);
		verify(ps).setNull(1, JdbcType.VARCHAR.TYPE_CODE);
	}

	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getString("column")).thenReturn("ONE");
		assertEquals(MyEnum.ONE, TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getString("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
		verify(rs, never()).wasNull();
	}

	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getString(1)).thenReturn("ONE");
		assertEquals(MyEnum.ONE, TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
		verify(rs, never()).wasNull();
	}

	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getString(1)).thenReturn("ONE");
		assertEquals(MyEnum.ONE, TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getString(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
		verify(cs, never()).wasNull();
	}

	enum MyEnum {
		ONE,
		TWO;
	}
}