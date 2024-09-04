package com.mawen.learn.mybatis.scripting;

import com.mawen.learn.mybatis.executor.parameter.ParameterHandler;
import com.mawen.learn.mybatis.mapping.BoundSql;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public interface LanguageDriver {

	ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

	SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);

	SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);
}
