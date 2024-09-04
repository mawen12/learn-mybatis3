package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mawen.learn.mybatis.builder.BuilderException;
import ognl.Ognl;
import ognl.OgnlException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public final class OgnlCache {

	private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();
	private static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();
	private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

	private OgnlCache() {
	}

	public static Object getValue(String expression, Object root) {
		try {
			Map context = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
			return Ognl.getValue(parseExpression(expression), context, root);
		}
		catch (OgnlException e) {
			throw new BuilderException("Error evaluating expression: " + expression + ". Cause: " + e, e);
		}
	}

	private static Object parseExpression(String expression) throws OgnlException {
		Object node = expressionCache.get(expression);
		if (node == null) {
			node = Ognl.parseExpression(expression);
			expressionCache.put(expression, node);
		}
		return node;
	}
}
