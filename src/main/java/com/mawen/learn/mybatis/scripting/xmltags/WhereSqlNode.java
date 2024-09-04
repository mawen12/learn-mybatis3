package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.Arrays;
import java.util.List;

import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class WhereSqlNode extends TrimSqlNode{

	private static List<String> prefixList = Arrays.asList("AND", "OR", "AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");

	public WhereSqlNode(Configuration configuration, SqlNode contents) {
		super(configuration, contents, "WHERE", prefixList, null, null);
	}
}
