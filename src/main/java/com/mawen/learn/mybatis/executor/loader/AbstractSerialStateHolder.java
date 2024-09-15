package com.mawen.learn.mybatis.executor.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.io.SerialFilterChecker;
import com.mawen.learn.mybatis.reflection.factory.ObjectFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public abstract class AbstractSerialStateHolder implements Externalizable {

	private static final long serialVersionUID = -7397040734036660621L;

	private static final ThreadLocal<ObjectOutputStream> stream = new ThreadLocal<>();

	private byte[] userBeanBytes = new byte[0];
	private Object userBean;
	private Map<String, ResultLoaderMap.LoadPair> unloadedProperties;
	private ObjectFactory objectFactory;
	private Class<?>[] constructorArgTypes;
	private Object[] constructorArgs;

	public AbstractSerialStateHolder() {}

	public AbstractSerialStateHolder(
			final Object userBean,
			final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
			final ObjectFactory objectFactory,
			List<Class<?>> constructorArgTypes,
			List<Object> constructorArgs) {
		this.userBean = userBean;
		this.unloadedProperties = unloadedProperties;
		this.objectFactory = objectFactory;
		this.constructorArgTypes = constructorArgTypes.toArray(new Class<?>[0]);
		this.constructorArgs = constructorArgs.toArray(new Object[0]);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		boolean firstRound = false;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream os = stream.get();

		if (os == null) {
			os = new ObjectOutputStream(baos);
			firstRound = true;
			stream.set(os);
		}

		os.writeObject(this.userBean);
		os.writeObject(this.unloadedProperties);
		os.writeObject(this.objectFactory);
		os.writeObject(this.constructorArgTypes);
		os.writeObject(this.constructorArgs);

		final byte[] bytes = baos.toByteArray();
		out.writeObject(bytes);

		if (firstRound) {
			stream.remove();
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		final Object data = in.readObject();
		if (data.getClass().isArray()) {
			this.userBeanBytes = (byte[]) data;
		}
		else {
			this.userBean = data;
		}
	}

	protected final Object readResolve() throws ObjectStreamException {
		if (this.userBean != null && this.userBeanBytes.length == 0) {
			return this.userBean;
		}

		SerialFilterChecker.check();

		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.userBeanBytes))) {
			this.userBean = in.readObject();
			this.unloadedProperties = (Map<String, ResultLoaderMap.LoadPair>) in.readObject();
			this.objectFactory = (ObjectFactory) in.readObject();
			this.constructorArgTypes = (Class<?>[]) in.readObject();
			this.constructorArgs = (Object[]) in.readObject();
		}
		catch (final IOException e) {
			throw (ObjectStreamException) new StreamCorruptedException().initCause(e);
		}
		catch (final ClassNotFoundException e) {
			throw (ObjectStreamException) new InvalidClassException(e.getLocalizedMessage()).initCause(e);
		}

		final Map<String, ResultLoaderMap.LoadPair> arrayProps = new HashMap<>(this.unloadedProperties);
		final List<Class<?>> arrayTypes = Arrays.asList(this.constructorArgTypes);
		final List<Object> arrayValues = Arrays.asList(this.constructorArgs);

		return this.createDeserializationProxy(userBean, arrayProps, objectFactory, arrayTypes, arrayValues);
	}

	protected abstract Object createDeserializationProxy(
			Object target,
			Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
			ObjectFactory objectFactory,
			List<Class<?>> constructorArgTypes,
			List<Object> constructorArgs
	);
}
