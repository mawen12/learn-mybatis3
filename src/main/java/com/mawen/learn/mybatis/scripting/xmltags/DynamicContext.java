package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.session.Configuration;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class DynamicContext {

	public static final String PARAMETER_OBJECT_KEY = "_parameter";

	public static final String DATABASE_ID_KEY = "_databaseId";

	static {
		OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
	}

	private final ContextMap bindings;
	private final StringJoiner sqlBuilder = new StringJoiner(" ");
	private int uniqueNumber = 0;

	public DynamicContext(Configuration configuration, Object parameterObject) {
		if (parameterObject != null && !(parameterObject instanceof Map)) {
			MetaObject metaObject = configuration.newMetaObject(parameterObject);
			boolean existsTypeHandler = configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
			this.bindings = new ContextMap(metaObject, existsTypeHandler);
		}
		else {
			this.bindings = new ContextMap(null, false);
		}

		bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
		bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
	}

	public Map<String, Object> getBindings() {
		return bindings;
	}

	public void bind(String name, Object value) {
		bindings.put(name, value);
	}

	public void appendSql(String sql) {
		sqlBuilder.add(sql);
	}

	public String getSql() {
		return sqlBuilder.toString().trim();
	}

	public int getUniqueNumber() {
		return uniqueNumber++;
	}

	static class ContextMap extends HashMap<String, Object> {
		private static final long serialVersionUID = 5184118395504112053L;

		private final MetaObject parameterMetaObject;
		private final boolean fallbackParameterObject;

		public ContextMap(MetaObject parameterMetaObject, boolean fallbackParameterObject) {
			this.parameterMetaObject = parameterMetaObject;
			this.fallbackParameterObject = fallbackParameterObject;
		}

		@Override
		public Object get(Object key) {
			String strKey = (String) key;
			if (super.containsKey(strKey)) {
				return super.get(strKey);
			}

			if (parameterMetaObject == null) {
				return null;
			}

			if (fallbackParameterObject && !parameterMetaObject.hasGetter(strKey)) {
				return parameterMetaObject.getOriginalObject();
			}
			else {
				return parameterMetaObject.getValue(strKey);
			}
		}
	}

	static class ContextAccessor implements PropertyAccessor {

		@Override
		public Object getProperty(Map context, Object target, Object name) throws OgnlException {
			Map map = (Map) target;
			Object result = map.get(name);
			if (map.containsKey(name) || result != null) {
				return result;
			}

			Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
			if (parameterObject instanceof Map) {
				return ((Map)parameterObject).get(name);
			}

			return null;
		}

		@Override
		public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
			Map<Object, Object> map = (Map<Object, Object>) target;
			map.put(name, value);
		}

		@Override
		public String getSourceAccessor(OgnlContext ognlContext, Object o, Object o1) {
			return null;
		}

		@Override
		public String getSourceSetter(OgnlContext ognlContext, Object o, Object o1) {
			return null;
		}
	}
}
