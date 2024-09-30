package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <pre>{@code
 * 	UPDATE author
 * 	<set>
 * 		<if test="username != null>
 * 			username = #{username},
 * 		</if>
 * 		<if test="password != null">
 * 		 	password = #{password}
 * 		</if>
 * 	</set>
 * 	WHERE id = #{id}
 * }</pre>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#trim-where-set">trim-where-set</a>
 */
class SetSqlNodeTest extends SqlNodeTest {

	private static final String FIRST_TEXT = " username = #{username},";
	private static final String SECOND_TEXT = " password = #{password}";

	private SqlNode sqlNode;

	@BeforeEach
	void setup() {
		SqlNode first = new IfSqlNode(new StaticTextSqlNode(FIRST_TEXT), "username != null");
		SqlNode second = new IfSqlNode(new StaticTextSqlNode(SECOND_TEXT), "password != null");
		SqlNode contents = new MixedSqlNode(Arrays.asList(first, second));

		this.sqlNode = new SetSqlNode(configuration, contents);
	}

	@Test
	@Override
	public void shouldApply() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>(){{
			put("username", "Jack");
			put("password", "***");
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("SET username = #{username}, password = #{password}");
	}

	@Test
	public void shouldAppendOnlyUsername() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>(){{
			put("username", "Jack");
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("SET username = #{username}");
	}

	@Test
	public void shouldAppendOnlyPassword() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>(){{
			put("password", "***");
		}});

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("SET password = #{password}");
	}

	@Test
	public void shouldAppendNone() throws Exception {
		when(context.getBindings()).thenReturn(new HashMap<>());

		boolean result = sqlNode.apply(context);

		assertTrue(result);
		verify(context).appendSql("");
	}


}