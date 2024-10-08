package com.mawen.learn.mybatis.domain.blog;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public class PostLiteId {
	private int id;

	public PostLiteId() {

	}

	public void setId(int id) {
		this.id = id;
	}

	public PostLiteId(int aId) {
		id = aId;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final PostLiteId that = (PostLiteId) o;

		if (id != that.id) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return id;
	}
}
