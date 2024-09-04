package com.mawen.learn.mybatis.cache.decorators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.cache.CacheException;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.io.SerialFilterChecker;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class SerializedCache implements Cache {

	private final Cache delegate;

	public SerializedCache(Cache delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public void putObject(Object key, Object object) {
		if (object == null || object instanceof Serializable) {
			delegate.putObject(key, serialize((Serializable) object));
		}
		else {
			throw new CacheException("SharedCache failed to make a copy of a non-serializable object: " + object);
		}
	}

	@Override
	public Object getObject(Object key) {
		Object object = delegate.getObject(key);
		return object == null ? null : deserialize((byte[]) object);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	private byte[] serialize(Serializable value) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(value);
			oos.flush();
			return bos.toByteArray();
		}
		catch (Exception e) {
			throw new CacheException("Error serializing object. Cause: " + e, e);
		}
	}

	private Serializable deserialize(byte[] value) {
		SerialFilterChecker.check();
		Serializable result;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(value);
		     ObjectInputStream ois = new CustomObjectInputStream(bis)) {
			result = (Serializable) ois.readObject();
		}
		catch (Exception e) {
			throw new CacheException("Error deserializing object. Cause: " + e, e);
		}
		return result;
	}

	public static class CustomObjectInputStream extends ObjectInputStream {

		public CustomObjectInputStream(InputStream in) throws IOException {
			super(in);
		}

		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			return Resources.classForName(desc.getName());
		}
	}
}
