package com.mawen.learn.mybatis.executor;

import java.util.ArrayList;
import java.util.List;

import com.mawen.learn.mybatis.mapping.MappedStatement;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class BatchResult {

	private final MappedStatement mappedStatement;
	private final String sql;
	private final List<Object> parameterObjects;

	private int[] updateCounts;

	public BatchResult(MappedStatement mappedStatement, String sql) {
		super();
		this.mappedStatement = mappedStatement;
		this.sql = sql;
		this.parameterObjects = new ArrayList<>();
	}

	public BatchResult(MappedStatement mappedStatement, String sql, Object parameterObject) {
		this(mappedStatement, sql);
		addParameterObject(parameterObject);
	}

	public MappedStatement getMappedStatement() {
		return mappedStatement;
	}

	public String getSql() {
		return sql;
	}

	public List<Object> getParameterObjects() {
		return parameterObjects;
	}

	public int[] getUpdateCounts() {
		return updateCounts;
	}

	public void setUpdateCounts(int[] updateCounts) {
		this.updateCounts = updateCounts;
	}

	public void addParameterObject(Object parameterObject) {
		this.parameterObjects.add(parameterObject);
	}
}


