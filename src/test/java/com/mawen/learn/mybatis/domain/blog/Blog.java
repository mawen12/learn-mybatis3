package com.mawen.learn.mybatis.domain.blog;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public class Blog {

	private int id;
	private String title;
	private Author author;
	private List<Post> posts;

	public Blog() {
	}

	public Blog(int id, String title, Author author, List<Post> posts) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.posts = posts;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	@Override
	public String toString() {
		return "Blog: " + id + " : " + title + " (" + author + ")";
	}
}
