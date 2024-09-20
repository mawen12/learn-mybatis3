package com.mawen.learn.mybatis.io;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public abstract class VFS {

	private static final Log log = LogFactory.getLog(VFS.class);

	public static final Class<?>[] IMPLEMENTATIONS = {JBoss6VFS.class, DefaultVFS.class};

	public static final List<Class<? extends VFS>> USER_IMPLEMENTATIONS = new ArrayList<>();

	public List<String> list(String path) throws IOException {
		List<String> names = new ArrayList<>();
		for (URL url : getResources(path)) {
			names.addAll(list(url, path));
		}
		return names;
	}

	public abstract boolean isValid();

	protected abstract List<String> list(URL url, String forPath) throws IOException;

	public static VFS getInstance() {
		return VFSHolder.INSTANCE;
	}

	public static void addImplClass(Class<? extends VFS> clazz) {
		if (clazz != null) {
			USER_IMPLEMENTATIONS.add(clazz);
		}
	}

	protected static List<URL> getResources(String path) throws IOException {
		return Collections.list(Thread.currentThread().getContextClassLoader().getResources(path));
	}

	protected static Class<?> getClass(String className) {
		try {
			return Thread.currentThread().getContextClassLoader().loadClass(className);
		}
		catch (ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.debug("Class not found: " + className);
			}
			return null;
		}
	}

	protected static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		if (clazz == null) {
			return null;
		}

		try {
			return clazz.getMethod(methodName, parameterTypes);
		}
		catch (SecurityException e) {
			log.error("Security exception looking for method " + clazz.getName() + "." + methodName + ". Cause: " + e);
			return null;
		}
		catch (NoSuchMethodException e) {
			log.error("Method not found " + clazz.getName() + "." + methodName + ". Cause: " + e);
			return null;
		}
	}

	protected static <T> T invoke(Method method, Object object, Object... parameters) throws IOException {
		try {
			return (T) method.invoke(object, parameters);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof IOException) {
				throw (IOException) e.getTargetException();
			}
			else {
				throw new RuntimeException(e);
			}
		}
	}


	private static class VFSHolder {

		static final VFS INSTANCE = createVFS();

		static VFS createVFS() {
			List<Class<? extends VFS>> impls = new ArrayList<>();
			impls.addAll(USER_IMPLEMENTATIONS);
			impls.addAll(Arrays.asList((Class<? extends VFS>[])IMPLEMENTATIONS));

			VFS vfs = null;
			for (int i = 0; vfs == null || !vfs.isValid(); i++) {
				Class<? extends VFS> impl = impls.get(i);
				try {
					vfs = impl.getDeclaredConstructor().newInstance();
					if (!vfs.isValid() && log.isDebugEnabled()) {
						log.debug("VFS implementation " + impl.getName() + " is not valid in this environment");
					}
				}
				catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
					log.error("Failed to instantiate " + impl, e);
					return null;
				}
			}

			return vfs;
		}

	}
}
