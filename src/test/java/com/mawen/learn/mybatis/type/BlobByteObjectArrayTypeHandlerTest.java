package com.mawen.learn.mybatis.type;

import java.io.ByteArrayInputStream;
import java.sql.Blob;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlobByteObjectArrayTypeHandlerTest extends BaseTypeHandlerTest {

	private static final TypeHandler<Byte[]> TYPE_HANDLER = new BlobByteObjectArrayTypeHandler();

	@Mock
	protected Blob blob;

	@Test
	@Override
	public void shouldSetParameter() throws Exception {
		ArgumentCaptor<Integer> positionCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<ByteArrayInputStream> byteArrayCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
		ArgumentCaptor<Integer> lengthCaptor = ArgumentCaptor.forClass(Integer.class);

		doNothing().when(ps).setBinaryStream(positionCaptor.capture(), byteArrayCaptor.capture(), lengthCaptor.capture());

		TYPE_HANDLER.setParameter(ps, 1, new Byte[]{1, 2}, null);

		assertEquals(1, positionCaptor.getValue());

		ByteArrayInputStream actualIn = byteArrayCaptor.getValue();
		assertEquals(1, actualIn.read());
		assertEquals(2, actualIn.read());
		assertEquals(-1, actualIn.read());

		assertEquals(2, lengthCaptor.getValue());
	}

	@Test
	@Override
	public void shouldGetResultFromResultSetByName() throws Exception {
		byte[] byteArray = new byte[]{1, 2};
		when(rs.getBlob("column")).thenReturn(blob);
		when(blob.length()).thenReturn((long) byteArray.length);
		when(blob.getBytes(1, 2)).thenReturn(byteArray);

		assertArrayEquals(new Byte[]{1, 2}, TYPE_HANDLER.getResult(rs, "column"));
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
		byte[] byteArray = new byte[]{1, 2};
		when(rs.getBlob(1)).thenReturn(blob);
		when(blob.length()).thenReturn((long) byteArray.length);
		when(blob.getBytes(1, 2)).thenReturn(byteArray);

		assertArrayEquals(new Byte[]{1, 2}, TYPE_HANDLER.getResult(rs, 1));
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
		byte[] byteArray = new byte[]{1, 2};
		when(cs.getBlob(1)).thenReturn(blob);
		when(blob.length()).thenReturn((long) byteArray.length);
		when(blob.getBytes(1, 2)).thenReturn(byteArray);

		assertArrayEquals(new Byte[]{1, 2}, TYPE_HANDLER.getResult(cs, 1));
	}

	@Test
	@Override
	public void shouldGetResultNullFromCallableStatement() throws Exception {
		when(cs.getBlob(1)).thenReturn(null);

		assertNull(TYPE_HANDLER.getResult(cs, 1));
	}
}