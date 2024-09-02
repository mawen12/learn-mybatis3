package com.mawen.learn.mybatis.type;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
class ByteArrayUtils {

	private ByteArrayUtils() {
	}

	static byte[] convertToPrimitiveArray(Byte[] objects) {
		final byte[] bytes = new byte[objects.length];
		for (int i = 0; i < objects.length; i++) {
			bytes[i] = objects[i];
		}
		return bytes;
	}

	static Byte[] convertToObjectArray(byte[] bytes) {
		final Byte[] objects= new Byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			objects[i] = bytes[i];
		}
		return objects;
	}

}
