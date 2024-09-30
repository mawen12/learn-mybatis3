package com.mawen.learn.mybatis.scripting.xmltags;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StaticTextSqlNodeTest extends SqlNodeTest {

	private static final String TEXT = "select 1 from dual";

	@Test
	@Override
	public void shouldApply() throws Exception {
		// given
		SqlNode sqlNode = new StaticTextSqlNode(TEXT);

		//when
		boolean result = sqlNode.apply(context);

		// then
		assertTrue(result);
		verify(context).appendSql(TEXT);
	}
}