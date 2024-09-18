package com.mawen.learn.mybatis.scripting.defaults;

import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/18
 */
public class RawLanguageDriver extends XMLLanguageDriver {

	@Override
	public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
		SqlSource source = super.createSqlSource(configuration, script, parameterType);
		checkIsNotDynamic(source);
		return source;
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
		SqlSource source = super.createSqlSource(configuration, script, parameterType);
		checkIsNotDynamic(source);
		return source;
	}

	private void checkIsNotDynamic(SqlSource sqlSource) {
		if (!RawSqlSource.class.equals(sqlSource.getClass())) {
			throw new BuilderException("Dynamic content is not allowed when using RAW language");
		}
	}
}
