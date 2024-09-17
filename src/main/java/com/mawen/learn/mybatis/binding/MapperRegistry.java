package com.mawen.learn.mybatis.binding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.builder.annotation.MapperAnnotationBuilder;
import com.mawen.learn.mybatis.io.ResolverUtil;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.SqlSession;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
public class MapperRegistry {

	private final Configuration configuration;
	private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

	public MapperRegistry(Configuration configuration) {
		this.configuration = configuration;
	}

	public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
		MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
		if (mapperProxyFactory == null) {
			throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
		}

		try {
			return mapperProxyFactory.newInstance(sqlSession);
		}
		catch (Exception e) {
			throw new BindingException("Error getting mapper instance. Cause: " + e, e);
		}
	}

	public Collection<Class<?>> getMappers() {
		return Collections.unmodifiableCollection(knownMappers.keySet());
	}

	public void addMappers(String packageName) {
		this.addMappers(packageName, Object.class);
	}

	public void addMappers(String packageName, Class<?> superType) {
		ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
		resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
		Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
		for (Class<?> mapperClass : mapperSet) {
			addMapper(mapperClass);
		}
	}

	public <T> void addMapper(Class<T> type) {
		if (type.isInterface()) {
			if (hasMapper(type)) {
				throw new BuilderException("Type " + type + " is already known to the MapperRegistry");
			}

			boolean loadCompleted = false;
			try {
				knownMappers.put(type, new MapperProxyFactory<>(type));
				MapperAnnotationBuilder parser = new MapperAnnotationBuilder(configuration, type);
				parser.parse();
				loadCompleted = true;
			}
			finally {
				if (!loadCompleted) {
					knownMappers.remove(type);
				}
			}
		}
	}

	public <T> boolean hasMapper(Class<T> type) {
		return knownMappers.containsKey(type);
	}
}
