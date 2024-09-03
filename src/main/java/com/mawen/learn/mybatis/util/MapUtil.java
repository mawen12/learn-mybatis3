package com.mawen.learn.mybatis.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class MapUtil {

	public static <K, V> V computIfAbsent(Map<K, V> map, K key, Function<K, V> mappingFunction) {
		V value = map.get(key);
		if (value != null) {
			return value;
		}
		return map.computeIfAbsent(key, mappingFunction);
	}

	public static <K, V> Map.Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleImmutableEntry<>(key, value);
	}

	private MapUtil() {}
}
