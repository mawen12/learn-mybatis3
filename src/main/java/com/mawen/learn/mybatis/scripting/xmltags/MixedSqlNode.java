package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class MixedSqlNode implements SqlNode{

	private final List<SqlNode> contents;

	public MixedSqlNode(List<SqlNode> contents) {
		this.contents = contents;
	}

	@Override
	public boolean apply(DynamicContext context) {
		contents.forEach(node -> node.apply(context));
		return true;
	}
}
