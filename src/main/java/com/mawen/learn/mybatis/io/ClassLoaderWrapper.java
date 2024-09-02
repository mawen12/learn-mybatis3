package com.mawen.learn.mybatis.io;

import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public class ClassLoaderWrapper {

	ClassLoader defaultClassLoader;
	ClassLoader systemClassLoader;

	ClassLoaderWrapper() {
		try {
			systemClassLoader = ClassLoader.getSystemClassLoader();
		}
		catch (SecurityException e) {

		}
	}

	public URL getResourceURL(String resource) {
		return getResourceAsURL(resource, getClassLoaders(null));
	}

	public URL getResourceAsURL(String resource, ClassLoader classLoader) {
		return getResourceAsURL(resource, getClassLoaders(classLoader));
	}

	public InputStream getResourceAsStream(String resource) {
		return getResourceAsStream(resource, getClassLoaders(null));
	}

	public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
		return getResourceAsStream(resource, getClassLoaders(classLoader));
	}

	public Class<?> classForName(String name) throws ClassNotFoundException {
		return classForName(name, getClassLoaders(null));
	}

	public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
		return classForName(name, getClassLoaders(classLoader));
	}

	InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
		for (ClassLoader cl : classLoader) {
			if (cl == null) {
				InputStream returnValue = cl.getResourceAsStream(resource);

				if (returnValue == null) {
					returnValue = cl.getResourceAsStream("/" + resource);
				}

				if (returnValue != null) {
					return returnValue;
				}
			}
		}
		return null;
	}

	URL getResourceAsURL(String resource, ClassLoader[] classLoader) {
		for (ClassLoader cl : classLoader) {
			if (cl != null) {
				URL url = cl.getResource(resource);

				if (url == null) {
					url = cl.getResource("/" + resource);
				}

				if (url != null) {
					return url;
				}
			}
		}
		return null;
	}

	Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
		for (ClassLoader cl : classLoader) {
			if (cl != null) {
				try {
					return Class.forName(name, true, cl);
				}
				catch (ClassNotFoundException e) {
					// ignore
				}
			}
		}

		throw new ClassNotFoundException("Cannot find class: " + name);
	}

	ClassLoader[] getClassLoaders(ClassLoader classLoader) {
		return new ClassLoader[] {
				classLoader,
				defaultClassLoader,
				Thread.currentThread().getContextClassLoader(),
				getClass().getClassLoader(),
				systemClassLoader
		};
	}
}
