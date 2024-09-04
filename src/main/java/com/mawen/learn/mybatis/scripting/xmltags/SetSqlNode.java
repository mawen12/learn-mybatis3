package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.Collections;
import java.util.List;

import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class SetSqlNode extends TrimSqlNode{

	private static final List<String> COMMA = Collections.singletonList(",");

	public SetSqlNode(Configuration configuration, SqlNode contents) {
		super(configuration, contents, "SET", COMMA, null, COMMA);
	}
}
