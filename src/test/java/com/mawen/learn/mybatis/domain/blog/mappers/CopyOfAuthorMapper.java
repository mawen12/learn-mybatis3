package com.mawen.learn.mybatis.domain.blog.mappers;

import java.util.List;

import com.mawen.learn.mybatis.domain.blog.Author;
import com.mawen.learn.mybatis.session.ResultHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public interface CopyOfAuthorMapper {

	List selectAllAuthors();

	void selectAllAuthors(ResultHandler handler);

	Author selectAuthor(int id);

	void selectAuthor(int id, ResultHandler handler);

	void insertAuthor(Author author);

	int deleteAuthor(int id);

	int updateAuthor(Author author);
}
