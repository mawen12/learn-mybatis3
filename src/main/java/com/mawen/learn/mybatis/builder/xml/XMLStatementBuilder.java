package com.mawen.learn.mybatis.builder.xml;

import java.util.List;
import java.util.Locale;

import com.mawen.learn.mybatis.builder.BaseBuilder;
import com.mawen.learn.mybatis.builder.MapperBuilderAssistant;
import com.mawen.learn.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.KeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.NoKeyGenerator;
import com.mawen.learn.mybatis.executor.keygen.SelectKeyGenerator;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.ResultSetType;
import com.mawen.learn.mybatis.mapping.SqlCommandType;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.mapping.StatementType;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.scripting.LanguageDriver;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class XMLStatementBuilder extends BaseBuilder {

	private final MapperBuilderAssistant builderAssistant;
	private final XNode context;
	private final String requiredDatabaseId;

	public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context) {
		this(configuration, builderAssistant, context, null);
	}

	public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context, String requiredDatabaseId) {
		super(configuration);
		this.builderAssistant = builderAssistant;
		this.context = context;
		this.requiredDatabaseId = requiredDatabaseId;
	}

	public void parseStatementNode() {
		String id = context.getStringAttribute("id");
		String databaseId = context.getStringAttribute("databaseId");

		if (!databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
			return;
		}

		String nodeName = context.getNode().getNodeName();
		SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
		boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
		boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
		boolean useCache = context.getBooleanAttribute("useCache", isSelect);
		boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);

		XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
		includeParser.applyIncludes(context.getNode());

		String parameterType = context.getStringAttribute("parameterType");
		Class<?> parameterTypeClass = resolveClass(parameterType);

		String lang = context.getStringAttribute("lang");
		LanguageDriver langDriver = getLanguageDriver(lang);
		processSelectKeyNodes(id, parameterTypeClass, langDriver);

		KeyGenerator keyGenerator;
		String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_PREFIX;
		keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
		if (configuration.hasKeyGenerator(keyStatementId)) {
			keyGenerator = configuration.getKeyGenerator(keyStatementId);
		}
		else {
			keyGenerator = context.getBooleanAttribute("useGeneratedKeys", configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
					? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
		}

		SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
		StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
		Integer fetchSize = context.getIntAttribute("fetchSize");
		Integer timeout = context.getIntAttribute("timeout");
		String parameterMap = context.getStringAttribute("parameterMap");
		String resultType = context.getStringAttribute("resultType");
		Class<?> resultTypeClas = resolveClass(resultType);
		String resultMap = context.getStringAttribute("resultMap");
		String resultSetType = context.getStringAttribute("resultSetType");
		ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);
		if (resultSetTypeEnum == null) {
			resultSetTypeEnum = configuration.getDefaultResultSetType();
		}
		String keyProperty = context.getStringAttribute("keyProperty");
		String keyColumn = context.getStringAttribute("keyColumn");
		String resultSets = context.getStringAttribute("resultSets");

		builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
				fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClas,
				resultSetTypeEnum, flushCache, useCache, resultOrdered, keyGenerator,
				keyProperty, keyColumn, databaseId, langDriver, resultSets);
	}

	private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
		List<XNode> selectKeyNodes = context.evalNodes("selectKey");
		if (configuration.getDatabaseId() != null) {
			parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver, configuration.getDatabaseId());
		}
		parseSelectKeyNodes(id, selectKeyNodes,parameterTypeClass,langDriver,null);
		removeSelectKeyNodes(selectKeyNodes);
	}

	private void parseSelectKeyNodes(String parentId, List<XNode> list, Class<?> parameterTypeClass, LanguageDriver langDriver, String skRequiredDatabaseId) {
		for (XNode nodeToHandle : list) {
			String id = parentId + SelectKeyGenerator.SELECT_KEY_PREFIX;
			String databaseId = nodeToHandle.getStringAttribute("databaseId");
			if (databaseIdMatchesCurrent(id, databaseId, skRequiredDatabaseId)) {
				parseSelectKeyNode(id, nodeToHandle, parameterTypeClass, langDriver, databaseId);
			}
		}
	}

	private void parseSelectKeyNode(String id, XNode nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver, String databaseId) {
		String resultType = nodeToHandle.getStringAttribute("resultType");
		Class<?> resultTypeClass = resolveClass(resultType);
		StatementType statementType = StatementType.valueOf(nodeToHandle.getStringAttribute("statementType", StatementType.PREPARED.toString()));
		String keyProperty = nodeToHandle.getStringAttribute("keyProperty");
		String keyColumn = nodeToHandle.getStringAttribute("keyColumn");
		boolean executeBefore = "BEFORE".equals(nodeToHandle.getStringAttribute("order", "AFTER"));

		boolean useCache = false;
		boolean resultOrdered = false;
		NoKeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
		Integer fetchSize = null;
		Integer timeout = null;
		boolean flushCache = false;
		String parameterMap = null;
		String resultMap = null;
		ResultSetType resultSetTypeEnum = null;

		SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
		SqlCommandType sqlCommandType = SqlCommandType.SELECT;

		builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
				fetchSize, timeout, parameterMap, parameterTypeClass, resultMap,
				resultTypeClass, resultSetTypeEnum, flushCache, useCache, resultOrdered,
				keyGenerator, keyProperty, keyColumn, databaseId, langDriver, null);

		id = builderAssistant.applyCurrentNamespace(id, false);

		MappedStatement keyStatement = configuration.getMappedStatement(id, false);
		configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
	}

	private void removeSelectKeyNodes(List<XNode> selectKeyNodes) {
		for (XNode nodeToHandle : selectKeyNodes) {
			nodeToHandle.getParent().getNode().removeChild(nodeToHandle.getNode());
		}
	}

	private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
		if (requiredDatabaseId != null) {
			return requiredDatabaseId.equals(databaseId);
		}

		if (databaseId != null) {
			return false;
		}

		id = builderAssistant.applyCurrentNamespace(id, false);
		if (!this.configuration.hasStatement(id, false)) {
			return true;
		}

		MappedStatement previous = this.configuration.getMappedStatement(id, false);
		return previous.getDatabaseId() == null;
	}

	private LanguageDriver getLanguageDriver(String lang) {
		Class<? extends LanguageDriver> langClass = null;
		if (lang != null) {
			langClass = resolveClass(lang);
		}
		return configuration.getLanguageDriver(langClass);
	}
}
