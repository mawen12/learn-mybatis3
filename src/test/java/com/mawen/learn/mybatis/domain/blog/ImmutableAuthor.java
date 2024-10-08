package com.mawen.learn.mybatis.domain.blog;

import java.io.Serializable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public class ImmutableAuthor implements Serializable {

	protected final int id;
	protected final String username;
	protected final String password;
	protected final String email;
	protected final String bio;
	protected final Section favouriteSection;

	public ImmutableAuthor(int id, String username, String password, String email, String bio, Section section) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.bio = bio;
		this.favouriteSection = section;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getBio() {
		return bio;
	}

	public Section getFavouriteSection() {
		return favouriteSection;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Author)) return false;

		Author author = (Author) o;

		if (id != author.id) return false;
		if (bio != null ? !bio.equals(author.bio) : author.bio != null) return false;
		if (email != null ? !email.equals(author.email) : author.email != null) return false;
		if (password != null ? !password.equals(author.password) : author.password != null) return false;
		if (username != null ? !username.equals(author.username) : author.username != null) return false;
		if (favouriteSection != null ? !favouriteSection.equals(author.favouriteSection) : author.favouriteSection != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = id;
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (email != null ? email.hashCode() : 0);
		result = 31 * result + (bio != null ? bio.hashCode() : 0);
		result = 31 * result + (favouriteSection != null ? favouriteSection.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return id + " " + username + " " + password + " " + email;
	}
}
