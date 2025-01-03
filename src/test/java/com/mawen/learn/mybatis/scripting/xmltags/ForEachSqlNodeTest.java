package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <pre>{@code
 * 	SELECT *
 * 	FROM POST
 * 	<where>
 * 	    <foreach item="item" index="index" collection="list" open="ID in (" separator="," close=")" nullable="true">
 * 	        #{item}
 * 	    </foreach>
 * 	</where>
 * }</pre>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#foreach">foreach</a>
 */
class ForEachSqlNodeTest extends SqlNodeTest{

	private SqlNode sqlNode;

	@BeforeEach
	void setup() {
		SqlNode contents = new StaticTextSqlNode("#{name}");
		this.sqlNode = new ForEachSqlNode(configuration, contents, "list", "index", "item", "ID in (", ")", ",");
	}

	@Test
	@Override
	public void shouldApply() throws Exception {
		ArgumentCaptor<String> bindKeyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> bindValueCaptor = ArgumentCaptor.forClass(Object.class);
		doNothing().when(context).bind(bindKeyCaptor.capture(), bindValueCaptor.capture());

		when(context.getBindings()).thenReturn(new HashMap<String, Object>() {{
			put("list", Arrays.asList("a", "b", "c"));
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("ID in (");
		verify(context).appendSql(")");

		List<String> allKeyValues = bindKeyCaptor.getAllValues();
		List<Object> allValValues = bindValueCaptor.getAllValues();
		assertEquals(Arrays.asList("index", "__frch__index_0", "item", "__frch__item_0",
				"index", "__frch__index_0", "item", "__frch__item_0",
				"index", "__frch__index_0", "item", "__frch__item_0"), allKeyValues);
		assertEquals(Arrays.asList(0, 0, "a", "a",
				1, 1, "b", "b",
				2, 2, "c", "c"), allValValues);
	}
}