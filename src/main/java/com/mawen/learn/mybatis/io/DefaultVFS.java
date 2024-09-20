package com.mawen.learn.mybatis.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/20
 */
public class DefaultVFS extends VFS {

	private static final Log log = LogFactory.getLog(DefaultVFS.class);

	private static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	protected List<String> list(URL url, String path) throws IOException {
		InputStream is = null;
		try {
			List<String> resources = new ArrayList<>();

			URL jarUrl = findJarForResource(url);
			if (jarUrl != null) {
				is = jarUrl.openStream();
				if (log.isDebugEnabled()) {
					log.debug("Listing " + url);
				}
				resources = listResource(new JarInputStream(is), path);
			}
			else {
				List<String> children = new ArrayList<>();
				try {
					if (isJar(url)) {
						is = url.openStream();
						try (JarInputStream jarInput = new JarInputStream(is)) {
							if (log.isDebugEnabled()) {
								log.debug("Listing " + url);
							}
							for (JarEntry entry; (entry = jarInput.getNextJarEntry()) != null; ) {
								if (log.isDebugEnabled()) {
									log.debug("Jar entry: " + entry.getName());
								}
								children.add(entry.getName());
							}
						}
					}
					else {
						is = url.openStream();
						List<String> lines = new ArrayList<>();
						try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
							for (String line; (line = reader.readLine()) != null; ) {
								if (log.isDebugEnabled()) {
									log.debug("Reader entry: " + line);
								}
								lines.add(line);

								if (getResources(path + "/" + line).isEmpty()) {
									lines.clear();
									break;
								}
							}
						}
						catch (InvalidPathException e) {
							lines.clear();
						}

						if (!lines.isEmpty()) {
							if (log.isDebugEnabled()) {
								log.debug("Listing " + url);
							}
							children.addAll(lines);
						}
					}
				}
				catch (FileNotFoundException e) {
					if ("file".equalsIgnoreCase(url.getProtocol())) {
						File file = new File(url.getFile());
						if (log.isDebugEnabled()) {
							log.debug("Listing directory " + file.getAbsolutePath());
						}

						if (file.isDirectory()) {
							if (log.isDebugEnabled()) {
								log.debug("Listing " + url);
							}
							children = Arrays.asList(file.list());
						}
					}
					else {
						throw e;
					}
				}

				String prefix = url.toExternalForm();
				if (!prefix.endsWith("/")) {
					prefix = prefix + "/";
				}

				for (String child : children) {
					String resourcePath = path + "/" + child;
					resources.add(resourcePath);
					URL childUrl = new URL(prefix + child);
					resources.addAll(list(childUrl, resourcePath));
				}
			}

			return resources;
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception ignored) {

				}
			}
		}
	}

	protected List<String> listResource(JarInputStream jar, String path) throws IOException {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		if (!path.endsWith("/")) {
			path += "/";
		}

		List<String> resources = new ArrayList<>();
		for (JarEntry entry; (entry = jar.getNextJarEntry()) != null; ) {
			if (!entry.isDirectory()) {
				StringBuilder name = new StringBuilder(entry.getName());
				if (name.charAt(0) != '/') {
					name.insert(0, '/');
				}

				if (name.indexOf(path) == 0) {
					if (log.isDebugEnabled()) {
						log.debug("Found resource: " + name);
					}

					resources.add(name.substring(1));
				}
			}
		}

		return resources;
	}

	protected URL findJarForResource(URL url) {
		if (log.isDebugEnabled()) {
			log.debug("Find JAR URL: " + url);
		}

		boolean continueLoop = true;
		while (continueLoop) {
			try {
				url = new URL(url.getFile());
				if (log.isDebugEnabled()) {
					log.debug("Inner URL: " + url);
				}
			}
			catch (MalformedURLException e) {
				continueLoop = false;
			}
		}

		StringBuilder jarUrl = new StringBuilder(url.toExternalForm());
		int index = jarUrl.lastIndexOf(".jar");
		if (index >= 0) {
			jarUrl.setLength(index + 4);
			if (log.isDebugEnabled()) {
				log.debug("Extracted JAR URL: " + url);
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("Not a JAR: " + jarUrl);
			}
			return null;
		}

		try {
			URL testUrl = new URL(jarUrl.toString());
			if (isJar(testUrl)) {
				return testUrl;
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Not a JAR: " + jarUrl);
				}
				jarUrl.replace(0, jarUrl.length(), testUrl.getFile());
				File file = new File(jarUrl.toString());

				if (!file.exists()) {
					try {
						file = new File(URLEncoder.encode(jarUrl.toString(), "UTF-8"));
					}
					catch (UnsupportedEncodingException e) {
						throw new RuntimeException("Unsupported encoding ? UTF-8? That's impossible.");
					}
				}

				if (file.exists()) {
					if (log.isDebugEnabled()) {
						log.debug("Trying real file: " + file.getAbsolutePath());
					}
					testUrl = file.toURI().toURL();
					if (isJar(testUrl)) {
						return testUrl;
					}
				}
			}
		}
		catch (MalformedURLException e) {
			log.warn("Invalid JAR URL: " + jarUrl);
		}

		if (log.isDebugEnabled()) {
			log.debug("Not a JAR: " + jarUrl);
		}
		return null;
	}

	protected String getPackagePath(String packageName) {
		return packageName == null ? null : packageName.replace('.', '/');
	}

	protected boolean isJar(URL url) {
		return isJar(url, new byte[JAR_MAGIC.length]);
	}

	protected boolean isJar(URL url, byte[] buffer) {
		try (InputStream is = url.openStream()) {
			is.read(buffer, 0, JAR_MAGIC.length);
			if (Arrays.equals(buffer, JAR_MAGIC)) {
				if (log.isDebugEnabled()) {
					log.debug("Found JAR: " + url);
				}
				return true;
			}
		}
		catch (Exception ignored) {
		}
		return false;
	}
}
