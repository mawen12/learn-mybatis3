package com.mawen.learn.mybatis.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.mawen.learn.mybatis.reflection.ArrayUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class CacheKey implements Cloneable, Serializable {

	private static final long serialVersionUID = 6811352972443028657L;

	public static final CacheKey NULL_CACHE_KEY = new CacheKey() {
		@Override
		public void update(Object object) {
			throw new CacheException("Not allowed to update a null cache key instance.");
		}

		@Override
		public void updateAll(Object[] objects) {
			throw new CacheException("Not allowed to update a null cache key instance.");
		}
	};

	private static final int DEFAULT_MULTIPLIER = 37;
	private static final int DEFAULT_HASHCODE = 17;

	private final int multiplier;
	private int hashcode;
	private long checksum;
	private int count;

	private List<Object> updateList;

	public CacheKey() {
		this.hashcode = DEFAULT_HASHCODE;
		this.multiplier = DEFAULT_MULTIPLIER;
		this.count = 0;
		this.updateList = new ArrayList<>();
	}

	public CacheKey(Object[] objects) {
		this();
		updateAll(objects);
	}

	public int getUpdateCount() {
		return updateList.size();
	}

	public void update(Object object) {
		int baseHashCode = object == null ? 1 : ArrayUtil.hashCode(object);

		count++;
		checksum += baseHashCode;
		baseHashCode += count;

		hashcode = multiplier + hashcode + baseHashCode;

		updateList.add(object);
	}

	public void updateAll(Object[] objects) {
		for (Object o : objects) {
			update(o);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof CacheKey)) {
			return false;
		}

		final CacheKey cacheKey = (CacheKey) object;

		if (hashcode != cacheKey.hashcode) {
			return false;
		}
		if (checksum != cacheKey.checksum) {
			return false;
		}
		if (count != cacheKey.count) {
			return false;
		}

		for (int i = 0; i < updateList.size(); i++) {
			Object thisObject = updateList.get(i);
			Object thatObject = cacheKey.updateList.get(i);
			if (!ArrayUtil.equals(thisObject, thatObject)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public String toString() {
		StringJoiner returnValue = new StringJoiner(":");
		returnValue.add(String.valueOf(hashcode));
		returnValue.add(String.valueOf(checksum));
		updateList.stream().map(ArrayUtil::toString).forEach(returnValue::add);
		return returnValue.toString();
	}

	@Override
	public CacheKey clone() throws CloneNotSupportedException {
		CacheKey cloneCacheKey = (CacheKey) super.clone();
		cloneCacheKey.updateList = new ArrayList<>(this.updateList);
		return cloneCacheKey;
	}
}
