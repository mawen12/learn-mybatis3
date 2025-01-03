package com.mawen.learn.mybatis.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import com.mawen.learn.mybatis.builder.xml.XMLConfigBuilder;
import com.mawen.learn.mybatis.exceptions.ExceptionFactory;
import com.mawen.learn.mybatis.executor.ErrorContext;
import com.mawen.learn.mybatis.session.defaults.DefaultSqlSessionFactory;

/**
 * 负责从输入流中读取并创建{@link SqlSessionFactory}。
 * Builder设计模式。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class SqlSessionFactoryBuilder {

	public SqlSessionFactory build(Reader reader) {
		return build(reader, null, null);
	}

	public SqlSessionFactory build(Reader reader, String environment) {
		return build(reader, environment, null);
	}

	public SqlSessionFactory build(Reader reader, Properties properties) {
		return build(reader, null, properties);
	}

	public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
		try {
			// 使用 XMLConfigBuilder 来解析全局配置文件
			XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
			return build(parser.parse());
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error building SqlSession.", e);
		}
		finally {
			ErrorContext.instance().reset();
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (IOException e) {
				// ignored
			}
		}
	}

	public SqlSessionFactory build(InputStream inputStream) {
		return build(inputStream, null, null);
	}

	public SqlSessionFactory build(InputStream inputStream, String environment) {
		return build(inputStream, environment, null);
	}

	public SqlSessionFactory build(InputStream inputStream, Properties properties) {
		return build(inputStream, null, properties);
	}

	public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
		try {
			XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
			return build(parser.parse());
		}
		catch (Exception e) {
			throw ExceptionFactory.wrapException("Error building SqlSession.", e);
		}
		finally {
			ErrorContext.instance().reset();
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (IOException e) {
				// Ignored
			}
		}
	}

	public SqlSessionFactory build(Configuration config) {
		return new DefaultSqlSessionFactory(config);
	}
}
