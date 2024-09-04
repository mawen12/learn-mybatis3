package com.mawen.learn.mybatis.executor.keygen;

import java.sql.Statement;

import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.mapping.MappedStatement;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public interface KeyGenerator {

	void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

	void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);
}
