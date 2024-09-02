package com.mawen.learn.mybatis.io;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class ResolverUtil<T> {

	private static final Log log = LogFactory.getLog(ResolverUtil.class);

	public interface Test {

		boolean matches(Class<?> type);
	}

	public static class IsA implements Test {

		private Class<?> parent;

		public IsA(Class<?> parent) {
			this.parent = parent;
		}

		@Override
		public boolean matches(Class<?> type) {
			return type != null && parent.isAssignableFrom(type);
		}

		@Override
		public String toString() {
			return "is assignable to " + parent.getSimpleName();
		}
	}

	public static class AnnotatedWith implements Test {

		private Class<? extends Annotation> annotation;

		public AnnotatedWith(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
		}

		@Override
		public boolean matches(Class<?> type) {
			return type != null && type.isAnnotationPresent(annotation);
		}

		@Override
		public String toString() {
			return "annotated with @" + annotation.getSimpleName();
		}
	}

	private Set<Class<? extends T>> matches = new HashSet<>();

	private ClassLoader classLoader;

	public Set<Class<? extends T>> getClasses() {
		return matches;
	}

	public ClassLoader getClassLoader() {
		return classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ResolverUtil<T> findImplementations(Class<?> parent, String... packageNames) {
		if (packageNames == null) {
			return this;
		}

		Test test = new IsA(parent);
		for (String pkg : packageNames) {
			find(test, pkg);
		}
		return this;
	}

	public ResolverUtil<T> find(Test test, String packageName) {
		String path = getPackagePath(packageName);

		try {
			List<String> children = VFS.getInstance().list(path);
			for (String child : children) {
				if (child.endsWith(".class")) {
					addIfMatching(test, child);
				}
			}
		}
		catch (IOException e) {
			log.error("Could not read package: " + packageName, e);
		}

		return this;
	}

	protected String getPackagePath(String packageName) {
		return packageName == null ? null : packageName.replace('.', '/');
	}

	protected void addIfMatching(Test test, String fqn) {
		try {
			String externalName = fqn.substring(0, fqn.indexOf('.')).replace('/', '.');
			ClassLoader loader = getClassLoader();
			if (log.isDebugEnabled()) {
				log.debug("Checking to see if class " + externalName + " matches criteria [" + test + "]");
			}

			Class<?> type = loader.loadClass(externalName);
			if (test.matches(type)) {
				matches.add((Class<T>) type);
			}
		}
		catch (Throwable e) {
			log.warn("Could not examine class '" + fqn + "' due to a " + e.getClass().getName() + " with message: " + e.getMessage());
		}
	}
}
