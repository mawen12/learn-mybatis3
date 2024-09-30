package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.mawen.learn.mybatis.domain.blog.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <pre>{@code
 *  SELECT *
 *  FROM BLOG
 *  WHERE state = 'active'
 *  <choose>
 * 		<when test="title != null">
 * 		 	AND title like #{title}
 * 		</when>
 * 		<when test="author != null && author.username != null">
 * 		 	AND author_name like #{author.username}
 * 		</when>
 * 		<otherwise>
 * 		 	AND featured = 1
 * 		</otherwise>
 *  </choose>
 * }</pre>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#choose-when-otherwise">choose</a>
 */
class ChooseSqlNodeTest extends SqlNodeTest{

	private static final String FIRST_TEXT = " AND title like #{title}";
	private static final String SECOND_TEXT = " AND author_name like #{author.username}";
	private static final String OTHERWISE_TEXT = " AND featured = 1";

	private SqlNode sqlNode;

	@BeforeEach
	void setup() {
		SqlNode first = new IfSqlNode(new StaticTextSqlNode(FIRST_TEXT), "title != null");
		SqlNode second = new IfSqlNode(new StaticTextSqlNode(SECOND_TEXT), "author != null && author.username != null");
		List<SqlNode> ifNodes = Arrays.asList(first, second);

		SqlNode defaultNode = new StaticTextSqlNode(OTHERWISE_TEXT);

		this.sqlNode = new ChooseSqlNode(ifNodes, defaultNode);
	}

	@Test
	@Override
	public void shouldApply() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>() {{
			put("title", "abc");
			put("author", new Author(1, "mybatis", "***", null, null, null));
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql(FIRST_TEXT);
	}

	@Test
	public void shouldAppendSecond() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>() {{
			put("author", new Author(1, "mybatis", "***", null, null, null));
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql(SECOND_TEXT);
	}

	@Test
	public void shouldAppendOtherwise() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>());

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql(OTHERWISE_TEXT);
	}




}