package com.mawen.learn.mybatis.scripting.xmltags;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class IfSqlNode implements SqlNode{

	private final ExpressionEvaluator evaluator;
	private final String test;
	private final SqlNode contents;

	public IfSqlNode(String test, SqlNode contents) {
		this.test = test;
		this.contents = contents;
		this.evaluator = new ExpressionEvaluator();
	}

	@Override
	public boolean apply(DynamicContext context) {
		if (evaluator.evaluateBoolean(test, context.getBindings())) {
			contents.apply(context);
			return true;
		}
		return false;
	}
}
