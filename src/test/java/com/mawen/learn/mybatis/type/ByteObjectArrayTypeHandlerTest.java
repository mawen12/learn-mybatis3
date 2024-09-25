package com.mawen.learn.mybatis.type;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ByteObjectArrayTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Byte[]> TYPE_HANDLER = new ByteObjectArrayTypeHandler();

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, new Byte[]{1, 2, 3}, null);
		verify(ps).setBytes(1, new byte[]{1, 2, 3});
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getBytes("column")).thenReturn(new byte[]{1, 2});
		assertThat(TYPE_HANDLER.getResult(rs, "column")).isEqualTo(new Byte[]{1, 2});
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getBytes("column")).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test

	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getBytes(1)).thenReturn(new byte[]{1, 2});
		assertThat(TYPE_HANDLER.getResult(rs, 1)).isEqualTo(new Byte[]{1, 2});
		verify(rs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getBytes(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getBytes(1)).thenReturn(new byte[]{1, 2});
		assertThat(TYPE_HANDLER.getResult(cs, 1)).isEqualTo(new Byte[]{1, 2});
		verify(cs, never()).wasNull();
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getBytes(1)).thenReturn(null);
		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}