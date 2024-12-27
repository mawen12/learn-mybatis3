package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <pre>{@code
 * 	SELECT *
 * 	FROM users
 * 	<where>
 * 	    <if test="id != null">
 * 			AND id = #{id}
 * 	    </if>
 * 	    <if test="name != null">
 * 			AND name = #{name}
 * 	    </if>
 * 	</where>
 * }</pre>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#trim-where-set">trim-where-set</a>
 */
class WhereSqlNodeTest extends SqlNodeTest {

	private static final String FIRST_TEXT = " AND id = #{id}";
	private static final String SECOND_TEXT = " AND name = #{name}";

	private SqlNode sqlNode;

	@BeforeEach
	void setup() {
		SqlNode first = new IfSqlNode(new StaticTextSqlNode(FIRST_TEXT), "id != null");
		SqlNode second = new IfSqlNode(new StaticTextSqlNode(SECOND_TEXT), "name != null");
		SqlNode contents = new MixedSqlNode(Arrays.asList(first, second));

		this.sqlNode = new WhereSqlNode(configuration, contents);
	}

	@Test
	@Override
	public void shouldApply() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<String, Object>() {{
			put("id", 1);
			put("name", "mybatis");
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("WHERE  id = #{id} AND name = #{name}");
	}

	@Test
	public void shouldAppendOnlyId() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<String, Object>() {{
			put("id", 1);
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("WHERE  id = #{id}");
	}

	@Test
	public void shouldAppendOnlyName() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<String, Object>() {{
			put("name", "mybatis");
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("WHERE  name = #{name}");
	}

	@Test
	public void shouldAppendNone() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>());

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("");
	}
}