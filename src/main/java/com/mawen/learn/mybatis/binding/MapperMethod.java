package com.mawen.learn.mybatis.binding;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mawen.learn.mybatis.annotations.Flush;
import com.mawen.learn.mybatis.annotations.MapKey;
import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.SqlCommandType;
import com.mawen.learn.mybatis.mapping.StatementType;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.ParamNameResolver;
import com.mawen.learn.mybatis.reflection.TypeParameterResolver;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ResultHandler;
import com.mawen.learn.mybatis.session.RowBounds;
import com.mawen.learn.mybatis.session.SqlSession;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class MapperMethod {

	private final SqlCommand command;
	private final MethodSignature method;

	public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
		this.command = new SqlCommand(configuration, mapperInterface, method);
		this.method = new MethodSignature(configuration, mapperInterface, method);
	}

	public Object execute(SqlSession sqlSession, Object[] args) {
		Object result;
		switch (command.getType()) {
			case INSERT: {
				Object param = method.convertArgsToSqlCommandParam(args);
				result = rowCountResult(sqlSession.insert(command.getName(), param));
				break;
			}
			case UPDATE: {
				Object param = method.convertArgsToSqlCommandParam(args);
				result = rowCountResult(sqlSession.update(command.getName(), param));
				break;
			}
			case DELETE: {
				Object param = method.convertArgsToSqlCommandParam(args);
				result = rowCountResult(sqlSession.delete(command.getName(), param));
				break;
			}
			case SELECT: {
				if (method.returnsVoid && method.hasResultHandler()) {
					executeWithResultHandler(sqlSession, args);
					result = null;
				}
				else if (method.returnsMany()) {
					result = executeForMany(sqlSession, args);
				}
				else if (method.returnsCursor()) {
					result = executeForCursor(sqlSession, args);
				}
				else {
					Object param = method.convertArgsToSqlCommandParam(args);
					result = sqlSession.selectOne(command.getName(), param);
					if (method.returnsOptional() && (result == null || !method.getReturnType().equals(result.getClass()))) {
						result = Optional.ofNullable(result);
					}
				}
				break;
			}
			case FLUSH:
				result = sqlSession.flushStatements();
				break;
			default:
				throw new BindingException("Unknown execution method for: " + command.getName());
		}

		if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
			throw new BindingException("Mapper method '" + command.getName() + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
		}
		return result;
	}

	private Object rowCountResult(int rowCount) {
		final Object result;
		if (method.returnsVoid) {
			result = null;
		}
		else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
			result = rowCount;
		}
		else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
			result = (long) rowCount;
		}
		else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
			result = rowCount > 0;
		}
		else {
			throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
		}
		return result;
	}

	private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
		MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
		if (!StatementType.CALLABLE.equals(ms.getStatementType()) && void.class.equals(ms.getResultMaps().get(0).getType())) {
			throw new BindingException("method " + command.getName() + " needs either a @ResultMap annotation, a @ResultType annotation,"
			                           + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
		}

		Object param = method.convertArgsToSqlCommandParam(args);
		if (method.hasRowBounds()) {
			RowBounds rowBounds = method.extractRowBounds(args);
			sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
		}
		else {
			sqlSession.select(command.getName(), param, method.extractResultHandler(args));
		}
	}

	private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
		List<E> result;
		Object param = method.convertArgsToSqlCommandParam(args);
		if (method.hasRowBounds()) {
			RowBounds rowBounds = method.extractRowBounds(args);
			result = sqlSession.selectList(command.getName(), param, rowBounds);
		}
		else {
			result = sqlSession.selectList(command.getName(), param);
		}
		return result;
	}

	private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
		Cursor<T> result;
		Object param = method.convertArgsToSqlCommandParam(args);
		if (method.hasRowBounds()) {
			RowBounds rowBounds = method.extractRowBounds(args);
			result = sqlSession.selectCursor(command.getName(), param, rowBounds);
		}
		else {
			result = sqlSession.selectCursor(command.getName(), param);
		}
		return result;
	}

	private <E> Object convertToDeclaredCollection(Configuration configuration, List<E> list) {
		Object collection = configuration.getObjectFactory().create(method.getReturnType());
		MetaObject metaObject = configuration.newMetaObject(collection);
		metaObject.addAll(list);
		return collection;
	}

	private <E> Object convertToArray(List<E> list) {
		Class<?> arrayComponentType = method.getReturnType().getComponentType();
		Object array = Array.newInstance(arrayComponentType, list.size());
		if (arrayComponentType.isPrimitive()) {
			for (int i = 0; i < list.size(); i++) {
				Array.set(array, i, list.get(i));
			}
			return array;
		}
		else {
			return list.toArray((E[]) array);
		}
	}

	private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
		Map<K, V> result;
		Object param = method.convertArgsToSqlCommandParam(args);
		if (method.hasRowBounds()) {
			RowBounds rowBounds = method.extractRowBounds(args);
			result = sqlSession.selectMap(command.getName(), param, method.getMapKey(), rowBounds);
		}
		else {
			result = sqlSession.selectMap(command.getName(), param, method.getMapKey());
		}
		return result;
	}

	public static class ParamMap<V> extends HashMap<String, V> {

		private static final long serialVersionUID = 2027656203126050276L;

		@Override
		public V get(Object key) {
			if (!super.containsKey(key)) {
				throw new BuilderException("Parameter '" + key + "' not found. Available parameters are " + keySet());
			}
			return super.get(key);
		}
	}

	public static class SqlCommand {

		private final String name;
		private final SqlCommandType type;

		public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
			final String methodName = method.getName();
			final Class<?> declaringClass = method.getDeclaringClass();

			MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass, configuration);
			if (ms == null) {
				if (method.getAnnotation(Flush.class) != null) {
					name = null;
					type = SqlCommandType.FLUSH;
				}
				else {
					throw new BindingException("Invalid bound statement (not found): " + mapperInterface.getName() + "." + methodName);
				}
			}
			else {
				name = ms.getId();
				type = ms.getSqlCommandType();
				if (type == SqlCommandType.UNKNOWN) {
					throw new BindingException("Unknown execution method for: " + name);
				}
			}
		}

		public String getName() {
			return name;
		}

		public SqlCommandType getType() {
			return type;
		}

		private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName, Class<?> declaringClass, Configuration configuration) {
			String statementId = mapperInterface.getName() + "." + methodName;
			if (configuration.hasStatement(statementId)) {
				return configuration.getMappedStatement(statementId);
			}
			else if (mapperInterface.equals(declaringClass)) {
				return null;
			}

			for (Class<?> superInterface : mapperInterface.getInterfaces()) {
				if (declaringClass.isAssignableFrom(superInterface)) {
					MappedStatement ms = resolveMappedStatement(superInterface, methodName, declaringClass, configuration);
					if (ms != null) {
						return ms;
					}
				}
			}
			return null;
		}
	}

	public static class MethodSignature {

		private final boolean returnsMany;
		private final boolean returnsMap;
		private final boolean returnsVoid;
		private final boolean returnsCursor;
		private final boolean returnsOptional;
		private final Class<?> returnType;
		private final String mapKey;
		private final Integer resultHandlerIndex;
		private final Integer rowBoundsIndex;
		private final ParamNameResolver paramNameResolver;

		public MethodSignature(Configuration configuration, Class<?> mappedInterface, Method method) {
			Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mappedInterface);
			if (resolvedReturnType instanceof Class<?>) {
				this.returnType = (Class<?>) resolvedReturnType;
			}
			else if (resolvedReturnType instanceof ParameterizedType) {
				this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
			}
			else {
				this.returnType = method.getReturnType();
			}

			this.returnsVoid = void.class.equals(this.returnType);
			this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
			this.returnsCursor = Cursor.class.equals(this.returnType);
			this.returnsOptional = Optional.class.equals(this.returnType);
			this.mapKey = getMapKey(method);
			this.returnsMap = this.mapKey != null;
			this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
			this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
			this.paramNameResolver = new ParamNameResolver(configuration, method);
		}

		public Object convertArgsToSqlCommandParam(Object[] args) {
			return paramNameResolver.getNamedParams(args);
		}

		public boolean hasRowBounds() {
			return rowBoundsIndex != null;
		}

		public RowBounds extractRowBounds(Object[] args) {
			return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
		}

		public boolean hasResultHandler() {
			return resultHandlerIndex != null;
		}

		public ResultHandler extractResultHandler(Object[] args) {
			return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
		}

		public Class<?> getReturnType() {
			return returnType;
		}

		public boolean returnsMany() {
			return returnsMany;
		}

		public boolean returnsMap() {
			return returnsMap;
		}

		public boolean returnsVoid() {
			return returnsVoid;
		}

		public boolean returnsCursor() {
			return returnsCursor;
		}

		public boolean returnsOptional() {
			return returnsOptional;
		}

		private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
			Integer index = null;
			Class<?>[] argTypes = method.getParameterTypes();
			for (int i = 0; i < argTypes.length; i++) {
				if (paramType.isAssignableFrom(argTypes[i])) {
					if (index == null) {
						index = i;
					}
					else {
						throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
					}
				}
			}
			return index;
		}

		public String getMapKey() {
			return mapKey;
		}

		private String getMapKey(Method method) {
			String mapKey = null;
			if (Map.class.isAssignableFrom(method.getReturnType())) {
				MapKey annotation = method.getAnnotation(MapKey.class);
				if (annotation != null) {
					mapKey = annotation.value();
				}
			}
			return mapKey;
		}

	}
}
