package com.mawen.learn.mybatis.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mawen.learn.mybatis.cache.Cache;
import com.mawen.learn.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.NoKeyGenerator;
import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.scripting.LanguageDriver;
import com.mawen.learn.mybatis.session.Configuration;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
@Getter
public final class MappedStatement {

	private String resource;
	private Configuration configuration;
	private String id;
	private Integer fetchSize;
	private Integer timeout;
	private StatementType statementType;
	private ResultSetType resultSetType;
	private SqlSource sqlSource;
	private Cache cache;
	private ParameterMap parameterMap;
	private List<ResultMap> resultMaps;
	private boolean flushCacheRequired;
	private boolean useCache;
	private boolean resultOrdered;
	private SqlCommandType sqlCommandType;
	private KeyGenerator keyGenerator;
	private String[] keyProperties;
	private String[] keyColumns;
	private boolean hasNestedResultMaps;
	private String databaseId;
	private Log statementLog;
	private LanguageDriver lang;
	private String[] resultSets;

	MappedStatement() {}

	public static class Builder {
		private MappedStatement mappedStatement = new MappedStatement();

		public Builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
			mappedStatement.configuration = configuration;
			mappedStatement.id = id;
			mappedStatement.sqlSource = sqlSource;
			mappedStatement.statementType = StatementType.PREPARED;
			mappedStatement.resultSetType = ResultSetType.DEFAULT;
			mappedStatement.parameterMap = new ParameterMap.Builder(configuration, "defaultParameterMap", null, new ArrayList<>()).build();
			mappedStatement.resultMaps = new ArrayList<>();
			mappedStatement.sqlCommandType = sqlCommandType;
			mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;

			String logId = id;
			if (configuration.getLogPrefix() != null) {
				logId = configuration.getLogPrefix() + id;
			}

			mappedStatement.statementLog = LogFactory.getLog(logId);
			mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
		}

		public Builder resource(String resource) {
			mappedStatement.resource = resource;
			return this;
		}

		public String id() {
			return mappedStatement.id;
		}

		public Builder parameterMap(ParameterMap parameterMap) {
			mappedStatement.parameterMap = parameterMap;
			return this;
		}

		public Builder resultMaps(List<ResultMap> resultMaps) {
			mappedStatement.resultMaps = resultMaps;
			for (ResultMap resultMap : resultMaps) {
				mappedStatement.hasNestedResultMaps = mappedStatement.hasNestedResultMaps || resultMap.hasNestedResultMaps();
			}
			return this;
		}

		public Builder fetchSize(Integer fetchSize) {
			mappedStatement.fetchSize = fetchSize;
			return this;
		}

		public Builder timeout(Integer timeout) {
			mappedStatement.timeout = timeout;
			return this;
		}

		public Builder statementType(StatementType statementType) {
			mappedStatement.statementType = statementType;
			return this;
		}

		public Builder resultSetType(ResultSetType resultSetType) {
			mappedStatement.resultSetType = resultSetType;
			return this;
		}

		public Builder cache(Cache cache) {
			mappedStatement.cache = cache;
			return this;
		}

		public Builder flushCacheRequired(boolean flushCacheRequired) {
			mappedStatement.flushCacheRequired = flushCacheRequired;
			return this;
		}

		public Builder useCache(boolean useCache) {
			mappedStatement.useCache = useCache;
			return this;
		}

		public Builder resultOrdered(boolean resultOrdered) {
			mappedStatement.resultOrdered = resultOrdered;
			return this;
		}

		public Builder keyGenerator(KeyGenerator keyGenerator) {
			mappedStatement.keyGenerator = keyGenerator;
			return this;
		}

		public Builder keyProperty(String keyProperty) {
			mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
			return this;
		}

		public Builder keyColumn(String keyColumn) {
			mappedStatement.keyColumns = delimitedStringToArray(keyColumn);
			return this;
		}

		public Builder databaseId(String databaseId) {
			mappedStatement.databaseId = databaseId;
			return this;
		}

		public Builder lang(LanguageDriver driver) {
			mappedStatement.lang = driver;
			return this;
		}

		public Builder resultSets(String resultSet) {
			mappedStatement.resultSets = delimitedStringToArray(resultSet);
			return this;
		}

		public MappedStatement build() {
			assert mappedStatement.configuration != null;
			assert mappedStatement.id != null;
			assert mappedStatement.sqlSource != null;
			assert mappedStatement.lang != null;

			mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
			return mappedStatement;
		}
	}

	public BoundSql getBoundSql(Object parameterObject) {
		BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

		if (parameterMappings == null || parameterMappings.isEmpty()) {
			boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
		}

		for (ParameterMapping pm : boundSql.getParameterMappings()) {
			String rmId = pm.getResultMapId();
			if (rmId != null) {
				ResultMap rm = configuration.getResultMap(rmId);
				if (rm != null) {
					hasNestedResultMaps |= rm.hasNestedResultMaps();
				}
			}
		}

		return boundSql;
	}

	private static String[] delimitedStringToArray(String in) {
		if (in == null || in.trim().isEmpty()) {
			return null;
		}
		else {
			return in.split(",");
		}
	}
}
