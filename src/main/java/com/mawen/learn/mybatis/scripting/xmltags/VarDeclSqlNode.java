package com.mawen.learn.mybatis.scripting.xmltags;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class VarDeclSqlNode implements SqlNode {

	private final String name;
	private final String expression;

	public VarDeclSqlNode(String name, String exp) {
		this.name = name;
		this.expression = exp;
	}

	@Override
	public boolean apply(DynamicContext context) {
		final Object value = OgnlCache.getValue(expression, context.getBindings());
		context.bind(name, value);
		return true;
	}
}
