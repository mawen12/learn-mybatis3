package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TextSqlNodeTest extends SqlNodeTest {

	private static final String TEXT = "select 1 from dual";
	private static final String DYNAMIC_TEXT = "select * from user where id = ${id}";

	@Test
	@Override
	public void shouldApply() throws Exception {
		// given
		TextSqlNode sqlNode = new TextSqlNode(TEXT);

		// when
		boolean result = sqlNode.apply(context);

		// then
		assertTrue(result);
		assertFalse(sqlNode.isDynamic());
		verify(context).appendSql(TEXT);
	}

	@Test
	public void shouldApplyDynamic() {
		// given
		TextSqlNode sqlNode = new TextSqlNode(DYNAMIC_TEXT);
		when(context.getBindings()).thenReturn(new HashMap<>() {{
			put("id", 1);
		}});

		// when
		boolean result = sqlNode.apply(context);

		// then
		assertTrue(result);
		assertTrue(sqlNode.isDynamic());
		verify(context).appendSql("select * from user where id = 1");
	}
}