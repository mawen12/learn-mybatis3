package com.mawen.learn.mybatis.builder;

import com.mawen.learn.mybatis.cache.Cache;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class CacheRefResolver {

	private final MapperBuilderAssistant assistant;
	private final String cacheRefNamespace;

	public CacheRefResolver(MapperBuilderAssistant assistant, String cacheRefNamespace) {
		this.assistant = assistant;
		this.cacheRefNamespace = cacheRefNamespace;
	}

	public Cache resolveCacheRef() {
		return assistant.useCacheRef(cacheRefNamespace);
	}
}
