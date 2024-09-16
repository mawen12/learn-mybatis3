package com.mawen.learn.mybatis.executor.keygen;

import java.sql.Statement;
import java.util.List;

import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ExecutorType;
import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class SelectKeyGenerator implements KeyGenerator {

	public static final String SELECT_KEY_PREFIX = "!selectKey";

	private final boolean executeBefore;
	private final MappedStatement keyStatement;

	public SelectKeyGenerator(MappedStatement keyStatement, boolean executeBefore) {
		this.executeBefore = executeBefore;
		this.keyStatement = keyStatement;
	}

	@Override
	public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
		if (executeBefore) {
			processGeneratedKeys(executor, ms, parameter);
		}
	}

	@Override
	public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
		if (!executeBefore) {
			processGeneratedKeys(executor, ms, parameter);
		}
	}

	private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
		try {
			if (parameter != null && keyStatement != null && keyStatement.getKeProperties() != null) {
				String[] keyProperties = keyStatement.getKeyProperties();
				final Configuration configuration = ms.getConfiguration();
				final MetaObject metaParam = configuration.newMetaObject(parameter);
				Executor keyExecutor = configuration.newExecutor(executor.getTransaction(), ExecutorType.SIMPLE);
				List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
				if (values.size() == 0) {
					throw new ExecutorException("SelectKey returned no data.");
				}
				else if (values.size() > 1) {
					throw new ExecutorException("SelectKey returned more than one data.");
				}
				else {
					MetaObject metaResult = configuration.newMetaObject(values.get(0));
					if (keyProperties.length == 1) {
						if (metaResult.hasGetter(keyProperties[0])) {
							setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
						}
						else {
							setValue(metaParam, keyProperties[0], values.get(0));
						}
					}
					else {
						handleMultipleProperties(keyProperties, metaParam, metaResult);
					}
				}
			}
		}
		catch (ExecutorException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ExecutorException("Error selecting key or setting result to parameter object, Cause: " + e, e);
		}
	}

	private void handleMultipleProperties(String[] keyProperties, MetaObject metaParam, MetaObject metaResult) {
		String[] keyColumns = keyStatement.getKeyColumns();
		if (keyColumns == null || keyColumns.length == 0) {
			for (String keyProperty : keyProperties) {
				setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
			}
		}
		else {
			if (keyColumns.length != keyProperties.length) {
				throw new ExecutorException("If SelectKey has key columns, the number must match the number of key properties.");
			}
			for (int i = 0; i < keyProperties.length; i++) {
				setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
			}
		}
	}

	private void setValue(MetaObject metaParam, String property, Object value) {
		if (metaParam.hasSetter(property)) {
			metaParam.setValue(property, value);
		}
		else {
			throw new ExecutorException("No setter found for the keyProperty '" + property + "' in '" + metaParam.getOriginalObject().getClass().getName() + ".");
		}
	}
}
