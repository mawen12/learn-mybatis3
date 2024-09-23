package com.mawen.learn.mybatis.domain.blog.mappers;

import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/21
 */
public interface BlogMapper {

	List<Map> selectAllPosts();

	List<Map> selectAllPosts(RowBounds rowBounds);

	List<Map> selectAllPosts(RowBounds rowBounds, Object param);
}
