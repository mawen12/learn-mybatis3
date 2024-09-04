package com.mawen.learn.mybatis.scripting.xmltags;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class StaticTextSqlNode implements SqlNode{

	private final String text;

	public StaticTextSqlNode(String text) {
		this.text = text;
	}

	@Override
	public boolean apply(DynamicContext context) {
		context.appendSql(text);
		return true;
	}
}
