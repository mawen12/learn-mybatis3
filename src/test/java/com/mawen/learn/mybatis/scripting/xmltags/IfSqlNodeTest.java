package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <pre>{@code
 * 	<if test="title != null>
 * 		AND title like #{title}
 * 	</if>
 * }</pre>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#if">if</a>
 */
class IfSqlNodeTest extends SqlNodeTest {

	private static final String CONDITION = "title != null";

	private static final String TEXT = "AND title like #{title}";

	@Test
	@Override
	public void shouldApply() throws Exception {
		// given
		SqlNode contents = new StaticTextSqlNode(TEXT);
		SqlNode sqlNode = new IfSqlNode(contents, CONDITION);
		when(context.getBindings()).thenReturn(new HashMap<>() {{
			put("title", "ENGLISH");
		}});

		// when
		boolean result = sqlNode.apply(context);

		// then
		assertTrue(result);
		verify(context).appendSql(TEXT);
	}

	@Test
	public void shouldApplyFailed() {

		// given
		SqlNode contents = new StaticTextSqlNode(TEXT);
		SqlNode sqlNode = new IfSqlNode(contents, CONDITION);
		when(context.getBindings()).thenReturn(new HashMap<>() {{
			put("title", null);
		}});

		// when
		boolean result = sqlNode.apply(context);

		// then
		assertFalse(result);
		verify(context, never()).appendSql(TEXT);
	}
}