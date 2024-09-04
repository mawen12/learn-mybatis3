package com.mawen.learn.mybatis.executor.keygen;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mawen.learn.mybatis.executor.Executor;
import com.mawen.learn.mybatis.executor.ExecutorException;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.reflection.ArrayUtil;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ParamNameResolver;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.TypeHandler;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import com.mawen.learn.mybatis.util.MapUtil;
import sun.security.krb5.Config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class Jdbc3KeyGenerator implements KeyGenerator {

	public static final Jdbc3KeyGenerator INSTANCE = new Jdbc3KeyGenerator();

	private static final String SECOND_GENERIC_PARAM_NAME = ParamNameResolver.GENERIC_NAME_PREFIX + "2";

	private static final String MSG_TOO_MANY_KEYS = "Too many keys are generated. There are only %d target object."
			+ "You either specified a wrong 'keyProperty' or encountered a drive bug like #1523.";

	@Override
	public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
		// Do nothing
	}

	@Override
	public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
		processBatch(ms, stmt, parameter);
	}

	public void processBatch(MappedStatement ms, Statement stmt, Object parameter) {
		final String[] keyProperties = ms.getKeyProperties();
		if (keyProperties == null || keyProperties.length == 0) {
			return;
		}

		try (ResultSet rs = stmt.getGeneratedKeys()) {
			final ResultSetMetaData rsmd = rs.getMetaData();
			final Configuration configuration = ms.getConfiguration();
			if (rsmd.getColumnCount() < keyProperties.length) {
				// Error?
			}
			else {
				assignKeys(configuration, rs, rsmd, keyProperties, parameter);
			}
		}
		catch (Exception e) {
			throw new ExecutorException("Error getting generated key or setting result to parameter object. Cause: " + e, e);
		}
	}

	private void assignKeys(Configuration configuration, ResultSet rs, ResultSetMetaData rsmd, String[] keyProperties, Object parameter) throws SQLException {
		if (parameter instanceof ParamMap || parameter instanceof StrictMap) {
			assignKeyToParamMap(configuration, rs, rsmd, keyProperties, (Map<String, ?>) parameter);
		}
		else if (parameter instanceof ArrayList && !(ArrayList<?>)parameter.isEmpty()) {
			assignKeysToParamMapList(configuration, rs, rsmd, keyProperties, (ArrayList<ParamMap<?>>) parameter);
		}
		else {
			assignKeysToParam(configuration, rs, rsmd, keyProperties, parameter);
		}
	}

	private void assignKeysToParam(Configuration configuration, ResultSet rs, ResultSetMetaData rsmd, String[] keyProperties, Object parameter) {
		Collection<?> params = collectionize(parameter);
		if (params.isEmpty()) {
			return;
		}

		List<KeyAssigner> assignerList = new ArrayList<>();
		for (int i = 0; i < keyProperties.length; i++) {
			assignerList.add(new KeyAssigner(configuration, rsmd, i + 1, null, keyProperties[i]));
		}

		Iterator<?> iterator = params.iterator();
		while (iterator.hasNext()) {
			if (!iterator.hasNext()) {
				throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS, params.size()));
			}

			Object param = iterator.next();
			assignerList.forEach(x -> x.assign(rs, param));
		}
	}

	private void assignKeysToParamMapList(Configuration configuration, ResultSet rs, ResultSetMetaData rsmd, String[] keyProperties, List<ParamMap<?>> paramMapList) throws SQLException {
		Iterator<ParamMap<?>> iterator = paramMapList.iterator();
		List<KeyAssigner> assignerList = new ArrayList<>();
		long counter = 0;
		while (rs.next()) {
			if (!iterator.hasNext()) {
				throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS, counter));
			}

			ParamMap<?> paramMap = iterator.next();
			if (assignerList.isEmpty()) {
				for (int i = 0; i < keyProperties.length; i++) {
					assignerList.add(getAssignerForParamMap(configuration, rsmd, i + 1, paramMap, keyProperties[i], keyProperties, false).getValue());
				}
			}

			assignerList.forEach(x -> x.assign(rs, paramMap));
			counter++;
		}
	}

	private void assignKeyToParamMap(Configuration configuration, ResultSet rs, ResultSetMetaData rsmd, String[] keyProperties, Map<String, ?> paramMap) throws SQLException {
		if (paramMap.isEmpty()) {
			return;
		}

		Map<String, Map.Entry<Iterator<?>, List<KeyAssigner>>> assignerMap = new HashMap<>();
		for (int i = 0; i < keyProperties.length; i++) {
			Map.Entry<String, KeyAssigner> entry = getAssignerForParamMap(configuration, rsmd, i + 1, paramMap, keyProperties[i], keyProperties, true);
			Map.Entry<Iterator<?>, List<KeyAssigner>> iteratorPair = MapUtil.computIfAbsent(assignerMap, entry.getKey(), k -> MapUtil.entry(collectionize(paramMap.get(k)).iterator(), new ArrayList<>()));
			iteratorPair.getValue().add(entry.getValue());
		}

		long counter = 0;
		while (rs.next()) {
			for (Map.Entry<Iterator<?>, List<KeyAssigner>> pair : assignerMap.values()) {
				if (!pair.getKey().hasNext()) {
					throw new ExecutorException(String.format(MSG_TOO_MANY_KEYS, counter));
				}

				Object param = pair.getKey().next();
				pair.getValue().forEach(x -> x.assign(rs, param));
			}
			counter++;
		}
	}

	private Map.Entry<String, KeyAssigner> getAssignerForParamMap(Configuration configuration, ResultSetMetaData rsmd, int columnPosition, Map<String, ?> paramMap, String keyProperty, String[] keyProperties, boolean omitParamName) {
		Set<String> keySet = paramMap.keySet();
		boolean singleParam = !keySet.contains(SECOND_GENERIC_PARAM_NAME);
		int firstDot = keyProperty.indexOf('.');
		if (firstDot == -1) {
			if (singleParam) {
				return getAssignerForSingleParm(configuration, rsmd, columnPosition, paramMap, keyProperty, omitParamName);
			}
			throw new ExecutorException("Could not determine which parameter to assign generated keys to."
					+ "Note that when there are multiple parameters, 'keyProperty' must include the parameter name (e.g. 'param.id')."
					+ "Specified key properties are " + ArrayUtil.toString(keyProperties) + " and available parameters are " + keySet);
		}

		String paramName = keyProperty.substring(0, firstDot);
		if (keySet.contains(paramName)) {
			String argParamName = omitParamName ? null : paramName;
			String argKeyProperty = keyProperty.substring(firstDot + 1);
			return MapUtil.entry(paramName, new KeyAssigner(configuration, rsmd, columnPosition, argParamName, argKeyProperty));
		}
		else if (singleParam) {
			return getAssignerForSingleParm(configuration, rsmd, columnPosition, paramMap, keyProperty, omitParamName);
		}
		else {
			throw new ExecutorException("Could not find parameter '" + paramName + "'. "
					+ "Note that when there are multiple parameters, 'keyProperty' must include the parameter name (e.g. 'param.id')."
					+ "Specified key properties are " + ArrayUtil.toString(keyProperties) + " and available parameters are " + keySet);
		}
	}

	private Map.Entry<String, KeyAssigner> getAssignerForSingleParm(Configuration configuration, ResultSetMetaData rsmd, int columnPosition, Map<String, ?> paramMap, String keyProperty, boolean omitParamName) {
		String singleParamName = nameOfSingleParam(paramMap);
		String argParamName = omitParamName ? null : singleParamName;
		return MapUtil.entry(singleParamName, new KeyAssigner(configuration, rsmd, columnPosition, argParamName, keyProperty));
	}

	private static String nameOfSingleParam(Map<String, ?> paramMap) {
		return paramMap.keySet().iterator().next();
	}

	private static Collection<?> collectionize(Object param) {
		if (param instanceof Collection) {
			return (Collection<?>) param;
		}
		else if (param instanceof Object[]) {
			return Arrays.asList((Object[]) param);
		}
		else {
			return Arrays.asList(param);
		}
	}

	private class KeyAssigner {
		private final Configuration configuration;
		private final ResultSetMetaData rsmd;
		private final TypeHandlerRegistry typeHandlerRegistry;
		private final int columnPosition;
		private final String paramName;
		private final String propertyName;
		private TypeHandler<?> typeHandler;

		protected KeyAssigner(Configuration configuration, ResultSetMetaData rsmd, int columnPosition, String paramName, String propertyName) {
			super();
			this.configuration = configuration;
			this.rsmd = rsmd;
			this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			this.columnPosition = columnPosition;
			this.paramName = paramName;
			this.propertyName = propertyName;
		}

		protected void assign(ResultSet rs, Object param) {
			if (paramName != null) {
				param = param.get(paramName);
			}

			MetaObject metaParam = configuration.newMetaObject(param);
			try {
				if (typeHandler == null) {
					if (metaParam.hasSetter(propertyName)) {
						Class<?> propertyType = metaParam.getSetterType(propertyName);
						typeHandler = typeHandlerRegistry.getTypeHandler(propertyType, JdbcType.forCode(rsmd.getColumnType(columnPosition)));
					}
					else {
						throw new ExecutorException("No setter found for the keyProperty '" + propertyName + "' in '" + metaParam.getOriginalObject().getClass().getName() + "'.");
					}
				}

				if (typeHandler == null) {
					// Error?
				}
				else {
					Object value = typeHandler.getResult(rs, columnPosition);
					metaParam.setValue(propertyName, value);
				}
			}
			catch (SQLException e) {
				throw new ExecutorException("Error getting generated key or setting result to parameter object. Cause: " + e);
			}
		}
	}
}
