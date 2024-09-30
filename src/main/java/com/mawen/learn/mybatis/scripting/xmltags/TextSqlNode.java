package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.regex.Pattern;

import com.mawen.learn.mybatis.parsing.GenericTokenParser;
import com.mawen.learn.mybatis.parsing.TokenHandler;
import com.mawen.learn.mybatis.scripting.ScriptingException;
import com.mawen.learn.mybatis.type.SimpleTypeRegistry;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class TextSqlNode implements SqlNode {

	private final String text;
	private final Pattern injectionFilter;

	public TextSqlNode(String text) {
		this(text, null);
	}

	public TextSqlNode(String text, Pattern injectionFilter) {
		this.text = text;
		this.injectionFilter = injectionFilter;
	}

	public boolean isDynamic() {
		DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
		GenericTokenParser parser = createParser(checker);
		parser.parse(text);
		return checker.isDynamic();
	}

	@Override
	public boolean apply(DynamicContext context) {
		GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
		context.appendSql(parser.parse(text));
		return true;
	}

	private GenericTokenParser createParser(TokenHandler handler) {
		return new GenericTokenParser("${", "}", handler);
	}

	private static class BindingTokenParser implements TokenHandler {

		private DynamicContext context;
		private Pattern injectionFilter;

		public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
			this.context = context;
			this.injectionFilter = injectionFilter;
		}

		@Override
		public String handleToken(String content) {
			Object parameter = context.getBindings().get("_parameter");
			if (parameter == null) {
				context.getBindings().put("value", null);
			}
			else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
				context.getBindings().put("value", parameter);
			}

			Object value = OgnlCache.getValue(content, context.getBindings());
			String strValue = value == null ? "" : String.valueOf(value);
			checkInjection(strValue);
			return strValue;
		}

		private void checkInjection(String value) {
			if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
				throw new ScriptingException("Invalid input. Please conform to regex " + injectionFilter.pattern());
			}
		}
	}

	private static class DynamicCheckerTokenParser implements TokenHandler {

		private boolean isDynamic;

		public boolean isDynamic() {
			return isDynamic;
		}

		@Override
		public String handleToken(String content) {
			this.isDynamic = true;
			return null;
		}
	}
}
