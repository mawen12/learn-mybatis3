package com.mawen.learn.mybatis.cache;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public final class NullCacheKey extends CacheKey{

	private static final long serialVersionUID = -3651223936825638704L;

	public NullCacheKey() {
		super();
	}

	@Override
	public void update(Object object) {
		throw new CacheException("Not allowed to update a NullCacheKey instance.");
	}

	@Override
	public void updateAll(Object[] objects) {
		throw new CacheException("Not allowed to update a NullCacheKey instance.");
	}
}
