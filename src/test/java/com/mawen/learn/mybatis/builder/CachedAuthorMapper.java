package com.mawen.learn.mybatis.builder;

import com.mawen.learn.mybatis.domain.blog.Author;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public interface CachedAuthorMapper {

	Author selectAllAuthors();

	Author selectAuthorWithInlineParams(int id);

	void insertAuthor(Author author);

	boolean updateAuthor(Author author);

	boolean deleteAuthor(int id);
}
