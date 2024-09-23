package com.mawen.learn.mybatis.domain.blog.mappers;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.mawen.learn.mybatis.annotations.Select;
import com.mawen.learn.mybatis.domain.blog.Author;
import com.mawen.learn.mybatis.session.ResultHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public interface AuthorMapper {

	List<Author> selectAllAuthors();

	Set<Author> selectAllAuthorsSet();

	Vector<Author> selectAllAuthorsVector();

	LinkedList<Author> selectAllAuthorsLinkedList();

	Author[] selectAllAuthorsArray();

	void selectAllAuthors(ResultHandler resultHandler);

	Author selectAuthor(int id);

	LinkedHashMap<String, Object> selectAuthorLinkedHashMap(int id);

	void selectAuthor(int id, ResultHandler resultHandler);

	@Select("select")
	void selectAuthors(int id, ResultHandler resultHandler);

	void insertAuthor(Author author);

	int deleteAuthor(int id);

	int updateAuthor(Author author);
}
