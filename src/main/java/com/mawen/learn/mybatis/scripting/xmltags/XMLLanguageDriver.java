package com.mawen.learn.mybatis.scripting.xmltags;

import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.scripting.LanguageDriver;
import com.mawen.learn.mybatis.scripting.defaults.DefaultParameterHandler;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class XMLLanguageDriver implements LanguageDriver {

	@Override
	public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
		return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
		XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
		return builder.parseScriptNode();
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
		return null;
	}
}
