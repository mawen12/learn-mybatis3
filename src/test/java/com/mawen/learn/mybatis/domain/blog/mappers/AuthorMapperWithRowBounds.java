package com.mawen.learn.mybatis.domain.blog.mappers;

import com.mawen.learn.mybatis.annotations.Select;
import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public interface AuthorMapperWithRowBounds {

	@Select("select id, username, password, email, bio, favourite_section from author where id = #{id}")
	void selectAuthor(int id, RowBounds bounds1, RowBounds bounds2);
}
