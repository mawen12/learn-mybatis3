package com.mawen.learn.mybatis.domain.blog;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public class BlogLite {
	private int id;
	private List<PostLite> posts;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<PostLite> getPosts() {
		return posts;
	}

	public void setPosts(List<PostLite> posts) {
		this.posts = posts;
	}
}
