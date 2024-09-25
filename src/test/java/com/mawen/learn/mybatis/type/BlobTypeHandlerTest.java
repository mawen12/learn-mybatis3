package com.mawen.learn.mybatis.type;

import java.io.InputStream;
import java.sql.Blob;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlobTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<byte[]> TYPE_HANDLER = new BlobTypeHandler();

	@Mock
	private Blob blob;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		TYPE_HANDLER.setParameter(ps, 1, new byte[]{1, 2, 3}, null);
		verify(ps).setBinaryStream(eq(1), any(InputStream.class), eq(3));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		when(rs.getBlob("column")).thenReturn(blob);
		when(blob.length()).thenReturn(3L);
		when(blob.getBytes(1, 3)).thenReturn(new byte[]{1, 2, 3});

		assertArrayEquals(new byte[]{1, 2, 3}, TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByName() throws Exception {
		when(rs.getBlob("column")).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(rs, "column"));
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByPosition() throws Exception {
		when(rs.getBlob(1)).thenReturn(blob);
		when(blob.length()).thenReturn(3L);
		when(blob.getBytes(1, 3)).thenReturn(new byte[]{1, 2, 3});

		assertArrayEquals(new byte[]{1, 2, 3}, TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromResultSetByPosition() throws Exception {
		when(rs.getBlob(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(rs, 1));
	}

	@Test
	@Override
	public void shouldGetResultFromCallableStatement() throws Exception {
		when(cs.getBlob(1)).thenReturn(blob);
		when(blob.length()).thenReturn(3L);
		when(blob.getBytes(1, 3)).thenReturn(new byte[]{1, 2, 3});

		assertArrayEquals(new byte[]{1, 2, 3}, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getBlob(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}