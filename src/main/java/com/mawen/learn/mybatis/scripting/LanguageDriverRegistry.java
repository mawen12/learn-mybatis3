package com.mawen.learn.mybatis.scripting;

import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.mybatis.util.MapUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/18
 */
public class LanguageDriverRegistry {

	private final Map<Class<? extends LanguageDriver>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

	private Class<? extends LanguageDriver> defaultDriverClass;

	public void register(Class<? extends LanguageDriver> cls) {
		if (cls == null) {
			throw new IllegalArgumentException("null is not a valid language driver");
		}

		MapUtil.computIfAbsent(LANGUAGE_DRIVER_MAP, cls, k -> {
			try {
				return k.getDeclaredConstructor().newInstance();
			}
			catch (Exception e) {
				throw new ScriptingException("Failed to load language driver for " + cls.getName(), e);
			}
		});
	}

	public void register(LanguageDriver instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null is not a valid language driver");
		}

		Class<? extends LanguageDriver> cls = instance.getClass();
		if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
			LANGUAGE_DRIVER_MAP.put(cls, instance);
		}
	}

	public LanguageDriver getDriver(Class<? extends LanguageDriver> cls) {
		return LANGUAGE_DRIVER_MAP.get(cls);
	}

	public LanguageDriver getDefaultDriver() {
		return getDriver(getDefaultDriverClass());
	}

	public Class<? extends LanguageDriver> getDefaultDriverClass() {
		return defaultDriverClass;
	}

	public void setDefaultDriverClass(Class<? extends LanguageDriver> defaultDriverClass) {
		register(defaultDriverClass);
		this.defaultDriverClass = defaultDriverClass;
	}
}
