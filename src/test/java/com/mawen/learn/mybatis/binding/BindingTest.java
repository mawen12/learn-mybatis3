package com.mawen.learn.mybatis.binding;

import java.util.List;

import com.mawen.learn.mybatis.domain.blog.Post;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.session.SqlSession;
import com.mawen.learn.mybatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/11/11
 */
public class BindingTest {

	private static SqlSessionFactory sqlSessionFactory;

	@Test
	void shouldFindThreeSpecificPosts() {
		try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
			BoundAuthorMapper mapper = sqlSession.getMapper(BoundAuthorMapper.class);
			List<Post> posts = mapper.findThreeSpecificPosts(1, new RowBounds(1, 1), 3, 5);
			assertEquals(1, posts.size());
			assertEquals(3, posts.get(0).getId());
		}
	}
}
