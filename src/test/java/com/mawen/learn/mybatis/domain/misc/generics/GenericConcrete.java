package com.mawen.learn.mybatis.domain.misc.generics;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public class GenericConcrete extends GenericSubclass implements GenericInterface<Long> {

	private Long id;

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(String id) {
		this.id = Long.valueOf(id);
	}

	public void setId(Integer id) {
		this.id = (long)id;
	}
}
