package com.mawen.learn.mybatis.io;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class JBoss6VFS extends VFS{

	private static final Log log = LogFactory.getLog(JBoss6VFS.class);

	private static Boolean valid;

	static {
		initialize();
	}

	protected static synchronized void initialize() {
		if (valid == null) {
			valid = Boolean.TRUE;

			VFS.VFS = checkNotNull(getClass("org.jboss.vfs.VFS"));
			VirtualFile.VirtualFile = checkNotNull(getClass("org.jboss.vfs.VirtualFile"));

			VFS.getChild = checkNotNull(getMethod(VFS.VFS, "getChild", URL.class));
			VirtualFile.getChildrenRecursively = checkNotNull(getMethod(VirtualFile.VirtualFile, "getChildrenRecursively"));
			VirtualFile.getPathNameRelativeTo = checkNotNull(getMethod(VirtualFile.VirtualFile, "getPathNameRelativeTo", VirtualFile.VirtualFile));

			checkReturnType(VFS.getChild, VirtualFile.VirtualFile);
			checkReturnType(VirtualFile.getChildrenRecursively,List.class);
			checkReturnType(VirtualFile.getPathNameRelativeTo, String.class);
		}
	}


	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	protected List<String> list(URL url, String path) throws IOException {
		VirtualFile directory = VFS.getChild(url);
		if (directory == null) {
			return Collections.emptyList();
		}

		if (!path.endsWith("/")) {
			path += "/";
		}

		List<VirtualFile> children = directory.getChildren();
		List<String> names = new ArrayList<>(children.size());
		for (VirtualFile vf : children) {
			names.add(path + vf.getPathNameRelativeTo(directory));
		}

		return names;
	}

	protected static <T> T checkNotNull(T object) {
		if (object == null) {
			setInvalid();
		}
		return object;
	}

	protected static void checkReturnType(Method method, Class<?> expected) {
		if (method != null && !expected.isAssignableFrom(method.getReturnType())) {
			log.error("Method " + method.getClass().getName() + "." + method.getName() + "(..) should return " + expected.getName()
			+ " but returns " + method.getReturnType().getName() + " instead.");
			setInvalid();
		}
	}

	protected static void setInvalid() {
		if (JBoss6VFS.valid.booleanValue()) {
			log.debug("JBoss 6 VFS API is not available in this environment.");
			JBoss6VFS.valid = Boolean.FALSE;
		}
	}

	static class VirtualFile {
		static Class<?> VirtualFile;
		static Method getPathNameRelativeTo;
		static Method getChildrenRecursively;

		Object virtualFile;

		VirtualFile(Object virtualFile) {
			this.virtualFile = virtualFile;
		}

		String getPathNameRelativeTo(VirtualFile parent) {
			try {
				return invoke(getPathNameRelativeTo, virtualFile, parent.virtualFile);
			}
			catch (IOException e) {
				log.error("This should not be possible. VirtualFile.getPathNameRelativeTo() threw IOException.");
				return null;
			}
		}

		List<VirtualFile> getChildren() throws IOException {
			List<?> objects = invoke(getChildrenRecursively, virtualFile);
			List<VirtualFile> children = new ArrayList<>(objects.size());
			for (Object object : objects) {
				children.add(new VirtualFile(object));
			}
			return children;
		}
	}

	static class VFS {
		static Class<?> VFS;
		static Method getChild;

		private VFS() {}

		static VirtualFile getChild(URL url) throws IOException {
			Object o = invoke(getChild, VFS, url);
			return o == null ? null : new VirtualFile(o);
		}
	}
}
