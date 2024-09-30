package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MixedSqlNodeTest extends SqlNodeTest{

	private static final String FIRST_TEXT = "abc";
	private static final String SECOND_TEXT = "bcd";
	private SqlNode sqlNode;

	@BeforeEach
	void setup() {
		SqlNode first = new StaticTextSqlNode(FIRST_TEXT);
		SqlNode second = new StaticTextSqlNode(SECOND_TEXT);
		this.sqlNode = new MixedSqlNode(Arrays.asList(first, second));
	}

	@Test
	@Override
	public void shouldApply() throws Exception {
		sqlNode.apply(context);

		verify(context).appendSql("abc");
		verify(context).appendSql("bcd");
	}
}