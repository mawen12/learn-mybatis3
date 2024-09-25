package com.mawen.learn.mybatis.type;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlobInputStreamTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<InputStream> TYPE_HANDLER = new BlobInputStreamTypeHandler();

	@Mock
	private Blob blob;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		InputStream in = new ByteArrayInputStream("Hello".getBytes());
		TYPE_HANDLER.setParameter(ps, 1, in, null);
		verify(ps).setBlob(1, in);
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		InputStream in = new ByteArrayInputStream("Hello".getBytes());
		when(rs.getBlob("column")).thenReturn(blob);
		when(blob.getBinaryStream()).thenReturn(in);

		assertEquals(in, TYPE_HANDLER.getResult(rs, "column"));
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
		InputStream in = new ByteArrayInputStream("Hello".getBytes());
		when(rs.getBlob(1)).thenReturn(blob);
		when(blob.getBinaryStream()).thenReturn(in);

		assertEquals(in, TYPE_HANDLER.getResult(rs, 1));
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
		InputStream in = new ByteArrayInputStream("Hello".getBytes());
		when(cs.getBlob(1)).thenReturn(blob);
		when(blob.getBinaryStream()).thenReturn(in);

		assertEquals(in, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getBlob(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}