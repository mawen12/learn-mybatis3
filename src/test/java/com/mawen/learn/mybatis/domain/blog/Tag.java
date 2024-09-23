package com.mawen.learn.mybatis.domain.blog;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public class Tag {

	private int id;
	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Tag: " + id + " : " + name;
	}
}
