package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <pre>{@code
 * 	<bind name="pattern" value="'%' + _parameter.getTitle() + '%'" />
 * 	SELECT * FROM BLOG
 * 	WHERE title like #{pattern}
 * }</pre>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#bind">bind</a>
 */
class VarDeclSqlNodeTest extends SqlNodeTest {

	private SqlNode sqlNode;

	@BeforeEach
	void setup() {
		this.sqlNode = new VarDeclSqlNode("pattern", "'%' + _parameter.getTitle() + '%'");
	}

	@Test
	@Override
	public void shouldApply() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>() {{
			put("_parameter", new Bean("abc"));
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).bind("pattern", "%abc%");
	}

	private static class Bean {

		private String title;

		public Bean(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}
}