package com.mawen.learn.mybatis.type;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mawen.learn.mybatis.binding.MapperMethod;
import com.mawen.learn.mybatis.io.ResolverUtil;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public final class TypeHandlerRegistry {

	private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();

	private final Map<JdbcType, TypeHandler<?>> jdbcTypeHandlerMap = new EnumMap<>(JdbcType.class);
	private final Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap = new ConcurrentHashMap<>();
	private final Map<Class<?>, TypeHandler<?>> allTypeHandlersMap = new HashMap<>();
	private Class<? extends TypeHandler> defaultEnumTypeHandler = EnumTypeHandler.class;
	private final TypeHandler<Object> unknownTypeHandler;

	public TypeHandlerRegistry() {
		this(new Configuration());
	}

	public TypeHandlerRegistry(Configuration configuration) {
		this.unknownTypeHandler = new UnknownTypeHandler(configuration);

		register(Boolean.class, new BooleanTypeHandler());
		register(boolean.class, new BooleanTypeHandler());
		register(JdbcType.BOOLEAN, new BooleanTypeHandler());
		register(JdbcType.BIT, new BooleanTypeHandler());

		register(Byte.class, new ByteTypeHandler());
		register(byte.class, new ByteTypeHandler());
		register(JdbcType.TINYINT, new ByteTypeHandler());

		register(Short.class, new ShortTypeHandler());
		register(short.class, new ShortTypeHandler());
		register(JdbcType.SMALLINT, new ShortTypeHandler());

		register(Integer.class, new IntegerTypeHandler());
		register(int.class, new IntegerTypeHandler());
		register(JdbcType.INTEGER, new IntegerTypeHandler());

		register(Long.class, new LongTypeHandler());
		register(long.class, new LongTypeHandler());

		register(Float.class, new FloatTypeHandler());
		register(float.class, new FloatTypeHandler());
		register(JdbcType.FLOAT, new FloatTypeHandler());

		register(Double.class, new DoubleTypeHandler());
		register(double.class, new DoubleTypeHandler());
		register(JdbcType.DOUBLE, new DoubleTypeHandler());

		register(Reader.class, new ClobReaderTypeHandler());

		register(String.class, new StringTypeHandler());
		register(String.class, JdbcType.CHAR, new StringTypeHandler());
		register(String.class, JdbcType.CLOB, new ClobTypeHandler());
		register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
		register(String.class, JdbcType.LONGVARCHAR, new StringTypeHandler());
		register(String.class, JdbcType.NVARCHAR, new NStringTypeHandler());
		register(String.class, JdbcType.NCHAR, new NStringTypeHandler());
		register(String.class, JdbcType.NCLOB, new NClobTypeHandler());

		register(JdbcType.CHAR, new StringTypeHandler());
		register(JdbcType.VARCHAR, new StringTypeHandler());
		register(JdbcType.CLOB, new ClobTypeHandler());
		register(JdbcType.LONGVARCHAR, new StringTypeHandler());
		register(JdbcType.NVARCHAR, new NStringTypeHandler());
		register(JdbcType.NCHAR, new NStringTypeHandler());
		register(JdbcType.NCLOB, new NClobTypeHandler());

		register(Object.class, JdbcType.ARRAY, new ArrayTypeHandler());
		register(JdbcType.ARRAY, new ArrayTypeHandler());

		register(BigInteger.class, new BigIntegerTypeHandler());
		register(JdbcType.BIGINT, new LongTypeHandler());

		register(BigDecimal.class, new BigDecimalTypeHandler());
		register(JdbcType.REAL, new BigDecimalTypeHandler());
		register(JdbcType.DECIMAL, new BigDecimalTypeHandler());
		register(JdbcType.NUMERIC, new BigDecimalTypeHandler());

		register(InputStream.class, new BlobInputStreamTypeHandler());

		register(Byte[].class, new ByteObjectArrayTypeHandler());
		register(Byte[].class, JdbcType.BLOB, new BlobByteObjectArrayTypeHandler());
		register(Byte[].class, JdbcType.LONGVARBINARY, new BlobByteObjectArrayTypeHandler());
		register(byte[].class, new ByteArrayTypeHandler());
		register(byte[].class, JdbcType.BLOB, new BlobTypeHandler());
		register(byte[].class, JdbcType.LONGVARBINARY, new BlobTypeHandler());
		register(JdbcType.LONGVARBINARY, new BlobTypeHandler());
		register(JdbcType.BLOB, new BlobTypeHandler());

		register(Object.class, unknownTypeHandler);
		register(Object.class, JdbcType.OTHER, unknownTypeHandler);
		register(JdbcType.OTHER, unknownTypeHandler);

		register(Date.class, new DateTypeHandler());
		register(Date.class, JdbcType.DATE, new DateOnlyTypeHandler());
		register(Date.class, JdbcType.TIME, new TimeOnlyTypeHandler());
		register(JdbcType.TIMESTAMP, new DateTypeHandler());
		register(JdbcType.DATE, new DateOnlyTypeHandler());
		register(JdbcType.TIME, new TimeOnlyTypeHandler());

		register(java.sql.Date.class, new SqlDateTypeHandler());
		register(java.sql.Time.class, new SqlTimeTypeHandler());
		register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());

		register(String.class, JdbcType.SQLXML, new SqlxmlTypeHandler());

		register(Instant.class, new InstantTypeHandler());
		register(LocalDateTime.class, new LocalDateTimeTypeHandler());
		register(LocalDate.class, new LocalDateTypeHandler());
		register(LocalTime.class, new LocalTimeTypeHandler());
		register(OffsetDateTime.class, new OffsetDateTimeTypeHandler());
		register(OffsetTime.class, new OffsetTimeTypeHandler());
		register(ZonedDateTime.class, new ZonedDataTimeTypeHandler());
		register(Month.class, new MonthTypeHandler());
		register(Year.class, new YearTypeHandler());
		register(YearMonth.class, new YearMonthTypeHandler());
		register(JapaneseDate.class, new JapaneseDateTypeHandler());

		register(Character.class, new CharacterTypeHandler());
		register(char.class, new CharacterTypeHandler());
	}

	public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
		this.defaultEnumTypeHandler = typeHandler;
	}

	public boolean hasTypeHandler(Class<?> javaType) {
		return hasTypeHandler(javaType, null);
	}

	public boolean hasTypeHandler(TypeReference<?> javaTypeReference) {
		return hasTypeHandler(javaTypeReference, null);
	}

	public boolean hasTypeHandler(Class<?> javaType, JdbcType jdbcType) {
		return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
	}

	public boolean hasTypeHandler(TypeReference<?> javaTypeReference, JdbcType jdbcType) {
		return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
	}

	public TypeHandler<?> getMappingTypeHandler(Class<? extends TypeHandler<?>> handlerType) {
		return allTypeHandlersMap.get(handlerType);
	}

	public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
		return getTypeHandler((Type) type, null);
	}

	public TypeHandler<?> getTypeHandler(JdbcType jdbcType) {
		return jdbcTypeHandlerMap.get(jdbcType);
	}

	public <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
		return getTypeHandler((Type) type, jdbcType);
	}

	public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
		return getTypeHandler(javaTypeReference, null);
	}

	public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference, JdbcType jdbcType) {
		return getTypeHandler(javaTypeReference.getRawType(), jdbcType);
	}

	private <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
		if (MapperMethod.ParamMap.class.equals(type)) {
			return null;
		}
		Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);
		TypeHandler<?> handler = null;
		if (jdbcHandlerMap != null) {
			handler = jdbcHandlerMap.get(jdbcType);
			if (handler == null) {
				handler = jdbcHandlerMap.get(null);
			}
			if (handler == null) {
				handler = pickSoleHandler(jdbcHandlerMap);
			}
		}

		return (TypeHandler<T>) handler;
	}

	private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMap(Type type) {
		Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(type);
		if (jdbcHandlerMap != null) {
			return NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap) ? null : jdbcHandlerMap;
		}

		if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			if (Enum.class.isAssignableFrom(clazz)) {
				Class<?> enumClass = clazz.isAnonymousClass() ? clazz.getSuperclass() : clazz;
				jdbcHandlerMap = getJdbcHandlerMapForEnumInterface(enumClass, enumClass);
				if (jdbcHandlerMap == null) {
					register(enumClass, getInstance(enumClass, defaultEnumTypeHandler));
					return typeHandlerMap.get(enumClass);
				}
			}
			else {
				jdbcHandlerMap = getJdbcHandlerMapForSuperclass(clazz);
			}
		}

		typeHandlerMap.put(type, jdbcHandlerMap == null ? NULL_TYPE_HANDLER_MAP : jdbcHandlerMap);
		return jdbcHandlerMap;
	}

	private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForEnumInterface(Class<?> clazz, Class<?> enumClass) {
		for (Class<?> iface : clazz.getInterfaces()) {
			Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(iface);
			if (jdbcHandlerMap == null) {
				jdbcHandlerMap = getJdbcHandlerMapForEnumInterface(iface, enumClass);
			}

			if (jdbcHandlerMap != null) {
				Map<JdbcType, TypeHandler<?>> newMap = new HashMap<>();
				for (Map.Entry<JdbcType, TypeHandler<?>> entry : jdbcHandlerMap.entrySet()) {
					newMap.put(entry.getKey(), getInstance(enumClass, entry.getValue().getClass()));
				}
				return newMap;
			}
		}

		return null;
	}

	private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForSuperclass(Class<?> clazz) {
		Class<?> superclass = clazz.getSuperclass();
		if (superclass == null || Object.class.equals(superclass)) {
			return null;
		}

		Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(superclass);
		if (jdbcHandlerMap != null) {
			return jdbcHandlerMap;
		}
		else {
			return getJdbcHandlerMapForSuperclass(superclass);
		}
	}

	private TypeHandler<?> pickSoleHandler(Map<JdbcType, TypeHandler<?>> jdbcHandlerMap) {
		TypeHandler<?> soleHandler = null;
		for (TypeHandler<?> handler : jdbcTypeHandlerMap.values()) {
			if (soleHandler == null) {
				soleHandler = handler;
			}
			else if (!handler.getClass().equals(soleHandler.getClass())) {
				return null;
			}
		}
		return soleHandler;
	}

	public TypeHandler<Object> getUnknownTypeHandler() {
		return unknownTypeHandler;
	}

	public void register(JdbcType jdbcType, TypeHandler<?> typeHandler) {
		jdbcTypeHandlerMap.put(jdbcType, typeHandler);
	}

	public <T> void register(TypeHandler<T> typeHandler) {
		boolean mappedTypeFound = false;
		MappedTypes mappedTypes = typeHandler.getClass().getAnnotation(MappedTypes.class);
		if (mappedTypes != null) {
			for (Class<?> handledType : mappedTypes.value()) {
				register(handledType, typeHandler);
				mappedTypeFound = true;
			}
		}

		if (!mappedTypeFound && typeHandler instanceof TypeReference) {
			try {
				TypeReference<T> typeReference = (TypeReference<T>) typeHandler;
				register(typeReference.getRawType(), typeHandler);
				mappedTypeFound = true;
			}
			catch (Throwable e) {

			}
		}

		if (!mappedTypeFound) {
			register((Class<T>) null, typeHandler);
		}
	}

	public <T> void register(Class<?> javaType, TypeHandler<? extends T> typeHandler) {
		register((Type) javaType, typeHandler);
	}

	private <T> void register(Type javaType, TypeHandler<? extends T> typeHandler) {
		MappedJdbcTypes mappedJdbcTypes = typeHandler.getClass().getAnnotation(MappedJdbcTypes.class);
		if (mappedJdbcTypes != null) {
			for (JdbcType handleJdbcType : mappedJdbcTypes.value()) {
				register(javaType, handleJdbcType, typeHandler);
			}
			if (mappedJdbcTypes.includeNullJdbcType()) {
				register(javaType, null, typeHandler);
			}
		}
		else {
			register(javaType, null, typeHandler);
		}
	}

	public <T> void register(TypeReference<T> javaTypeReference, TypeHandler<? extends T> handler) {
		register(javaTypeReference.getRawType(), handler);
	}

	public <T> void register(Class<T> type, JdbcType jdbcType, TypeHandler<?> handler) {
		register((Type) type, jdbcType, handler);
	}

	private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> typeHandler) {
		if (javaType != null) {
			Map<JdbcType, TypeHandler<?>> map = typeHandlerMap.get(javaType);
			if (map == null || map == NULL_TYPE_HANDLER_MAP) {
				map = new HashMap<>();
			}
			map.put(jdbcType, typeHandler);
			typeHandlerMap.put(javaType, map);
		}
		allTypeHandlersMap.put(typeHandler.getClass(), typeHandler);
	}

	public void register(Class<?> typeHandlerClass) {
		boolean mappedTypeFound = false;
		MappedTypes mappedTypes = typeHandlerClass.getAnnotation(MappedTypes.class);
		if (mappedTypes != null) {
			for (Class<?> javaTypeClass : mappedTypes.value()) {
				register(javaTypeClass, typeHandlerClass);
				mappedTypeFound = true;
			}
		}

		if (!mappedTypeFound) {
			register(getInstance(null, typeHandlerClass));
		}
	}

	public void register(String javaTypeClassName, String typeHandlerClassName) throws ClassNotFoundException {
		register(Resources.classForName(javaTypeClassName), Resources.classForName(typeHandlerClassName));
	}

	public void register(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
		register(javaTypeClass, getInstance(javaTypeClass, typeHandlerClass));
	}

	public void register(Class<?> javaTypeClass, JdbcType jdbcType, Class<?> typeHandlerClass) {
		register(javaTypeClass, jdbcType, getInstance(javaTypeClass, typeHandlerClass));
	}

	public <T> TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
		if (javaTypeClass != null) {
			try {
				Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
				return (TypeHandler<T>) c.newInstance(javaTypeClass);
			}
			catch (NoSuchMethodException e) {
				// ignored
			}
			catch (Exception e) {
				throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
			}
		}

		try {
			Constructor<?> c = typeHandlerClass.getConstructor();
			return (TypeHandler<T>) c.newInstance();
		}
		catch (Exception e) {
			throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
		}
	}

	public void register(String packageName) {
		ResolverUtil<Class<?>> resolverUtil = new ResolverUtil();
		resolverUtil.find(new ResolverUtil.IsA(TypeHandler.class), packageName);
		Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
		for (Class<?> type : handlerSet) {
			if (!type.isAnonymousClass() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
				register(type);
			}
		}
	}

	public Collection<TypeHandler<?>> getTypeHandlers() {
		return Collections.unmodifiableCollection(allTypeHandlersMap.values());
	}
}
