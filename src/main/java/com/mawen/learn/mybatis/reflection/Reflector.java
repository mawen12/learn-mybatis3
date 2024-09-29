package com.mawen.learn.mybatis.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.ReflectPermission;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mawen.learn.mybatis.reflection.invoker.AmbiguousMethodInvoker;
import com.mawen.learn.mybatis.reflection.invoker.GetFieldInvoker;
import com.mawen.learn.mybatis.reflection.invoker.Invoker;
import com.mawen.learn.mybatis.reflection.invoker.MethodInvoker;
import com.mawen.learn.mybatis.reflection.invoker.SetFieldInvoker;
import com.mawen.learn.mybatis.reflection.property.PropertyNamer;
import com.mawen.learn.mybatis.util.MapUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class Reflector {

	private static final MethodHandle isRecordMethodHandle = getIsRecordMethodHandle();

	private final Class<?> type;
	private final String[] readablePropertyNames;
	private final String[] writablePropertyNames;
	private final Map<String, Invoker> setMethods = new HashMap<>();
	private final Map<String, Invoker> getMethods = new HashMap<>();
	private final Map<String, Class<?>> setTypes = new HashMap<>();
	private final Map<String, Class<?>> getTypes = new HashMap<>();
	private Constructor<?> defaultConstructor;

	private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

	public Reflector(Class<?> clazz) {
		this.type = clazz;
		addDefaultConstructor(clazz);

		Method[] classMethods = getClassMethods(clazz);
		if (isRecord(type)) {
			addRecordGetMethods(classMethods);
		}
		else {
			addGetMethods(classMethods);
			addSetMethods(classMethods);
			addFields(clazz);
		}

		readablePropertyNames = getMethods.keySet().toArray(new String[0]);
		writablePropertyNames = setMethods.keySet().toArray(new String[0]);
		for (String propName : readablePropertyNames) {
			caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
		}
		for (String propName : writablePropertyNames) {
			caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
		}
	}

	private void addRecordGetMethods(Method[] methods) {
		Arrays.stream(methods)
				.filter(m -> m.getParameterTypes().length == 0)
				.forEach(m -> addGetMethod(m.getName(), m, false));
	}

	private void addDefaultConstructor(Class<?> clazz) {
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();

		Arrays.stream(constructors)
				.filter(constructor -> constructor.getParameterTypes().length == 0)
				.findAny()
				.ifPresent(constructor -> this.defaultConstructor = constructor);
	}

	private void addGetMethods(Method[] methods) {
		Map<String, List<Method>> conflictingGetters = new HashMap<>();

		Arrays.stream(methods)
				.filter(m -> m.getParameters().length == 0)
				.filter(m -> PropertyNamer.isGetter(m.getName()))
				.forEach(m -> addMethodConflict(conflictingGetters, PropertyNamer.methodToProperty(m.getName()), m));

		resolveGetterConflicts(conflictingGetters);
	}

	private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
		for (Map.Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
			Method winner = null;
			String propName = entry.getKey();
			boolean isAmbiguous = false;

			for (Method candidate : entry.getValue()) {
				if (winner == null) {
					winner = candidate;
					continue;
				}

				Class<?> winnerType = winner.getReturnType();
				Class<?> candidateType = candidate.getReturnType();
				if (candidateType.equals(winnerType)) {
					if (!boolean.class.equals(candidateType)) {
						isAmbiguous = true;
						break;
					}
					else if (candidate.getName().startsWith("is")) {
						winner = candidate;
					}
				}
				else if (candidateType.isAssignableFrom(winnerType)) {
					// OK getter type is descendant
				}
				else if (winnerType.isAssignableFrom(candidateType)) {
					winner = candidate;
				}
				else {
					isAmbiguous = true;
					break;
				}
			}
			addGetMethod(propName, winner, isAmbiguous);
		}
	}

	private void addGetMethod(String name, Method method, boolean isAmbiguous) {
		MethodInvoker invoker = isAmbiguous ? new AmbiguousMethodInvoker(method,
				MessageFormat.format("Illegal overloaded getter method with ambiguous type for property ''{0}'' in class ''{1}''. This breaks the JavaBeans specification and can cause unpredictable results.",
						name, method.getDeclaringClass().getName()))
				: new MethodInvoker(method);

		getMethods.put(name, invoker);
		Type returnType = TypeParameterResolver.resolveReturnType(method, type);
		getTypes.put(name, typeToClass(returnType));
	}

	private void addSetMethods(Method[] methods) {
		Map<String, List<Method>> conflictingSetters = new HashMap<>();
		Arrays.stream(methods)
				.filter(m -> m.getParameterTypes().length == 1 && PropertyNamer.isSetter(m.getName()))
				.forEach(m -> addMethodConflict(conflictingSetters, PropertyNamer.methodToProperty(m.getName()), m));

		resolveSetterConflicts(conflictingSetters);
	}

	private void addMethodConflict(Map<String, List<Method>> conflictingGetters, String name, Method method) {
		if (isValidPropertyName(name)) {
			List<Method> list = MapUtil.computIfAbsent(conflictingGetters, name, k -> new ArrayList<>());
			list.add(method);
		}
	}

	private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
		for (Map.Entry<String, List<Method>> entry : conflictingSetters.entrySet()) {
			String propName = entry.getKey();
			List<Method> setters = entry.getValue();

			Class<?> getterType = getTypes.get(propName);
			boolean isGetterAmbiguous = getMethods.get(propName) instanceof AmbiguousMethodInvoker;
			boolean isSetterAmbiguous = false;

			Method match = null;
			for (Method setter : setters) {
				if (!isGetterAmbiguous && setter.getParameterTypes()[0].equals(getterType)) {
					match = setter;
					break;
				}

				if (!isSetterAmbiguous) {
					match = pickBetterSetter(match, setter, propName);
					isSetterAmbiguous = match == null;
				}
			}

			if (match != null) {
				addSetMethod(propName, match);
			}
		}
	}

	private Method pickBetterSetter(Method setter1, Method setter2, String property) {
		if (setter1 == null) {
			return setter2;
		}

		Class<?> paramType1 = setter1.getParameterTypes()[0];
		Class<?> paramType2 = setter2.getParameterTypes()[0];
		if (paramType1.isAssignableFrom(paramType2)) {
			return setter2;
		}
		else if (paramType2.isAssignableFrom(paramType1)) {
			return setter1;
		}

		MethodInvoker invoker = new AmbiguousMethodInvoker(setter1, MessageFormat.format("Ambiguous setters defined for property ''{0}'' in class ''{1}'' with types ''{2}'' and ''{3}''.",
				property, setter2.getDeclaringClass().getName(), paramType1.getName(), paramType2.getName()));
		setMethods.put(property, invoker);
		Type[] paramTypes = TypeParameterResolver.resolveParamTypes(setter1, type);
		setTypes.put(property, typeToClass(paramTypes[0]));
		return null;
	}

	private void addSetMethod(String name, Method method) {
		MethodInvoker invoker = new MethodInvoker(method);
		setMethods.put(name, invoker);
		Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, type);
		setTypes.put(name, typeToClass(paramTypes[0]));
	}

	private Class<?> typeToClass(Type src) {
		Class<?> result = null;
		if (src instanceof Class) {
			result = (Class<?>) src;
		}
		else if (src instanceof ParameterizedType) {
			result = (Class<?>) ((ParameterizedType) src).getRawType();
		}
		else if (src instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) src).getGenericComponentType();
			if (componentType instanceof Class) {
				result = Array.newInstance((Class<?>) componentType, 0).getClass();
			}
			else {
				Class<?> componentClass = typeToClass(componentType);
				result = Array.newInstance(componentClass, 0).getClass();
			}
		}

		if (result == null) {
			result = Object.class;
		}

		return result;
	}

	private void addFields(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!setMethods.containsKey(field.getName())) {
				int modifiers = field.getModifiers();
				if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
					addSetField(field);
				}
			}

			if (!getMethods.containsKey(field.getName())) {
				addGetField(field);
			}
		}

		if (clazz.getSuperclass() != null) {
			addFields(clazz.getSuperclass());
		}
	}

	private void addSetField(Field field) {
		if (isValidPropertyName(field.getName())) {
			setMethods.put(field.getName(), new SetFieldInvoker(field));
			Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
			setTypes.put(field.getName(), typeToClass(fieldType));
		}
	}

	private void addGetField(Field field) {
		if (isValidPropertyName(field.getName())) {
			getMethods.put(field.getName(), new GetFieldInvoker(field));
			Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
			getTypes.put(field.getName(), typeToClass(fieldType));
		}
	}

	private boolean isValidPropertyName(String name) {
		return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
	}

	private Method[] getClassMethods(Class<?> clazz) {
		Map<String, Method> uniqueMethods = new HashMap<>();
		Class<?> currentClass = clazz;
		while (currentClass != null && currentClass != Object.class) {
			addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

			Class<?>[] interfaces = currentClass.getInterfaces();
			for (Class<?> anInterface : interfaces) {
				addUniqueMethods(uniqueMethods, anInterface.getMethods());
			}

			currentClass = currentClass.getSuperclass();
		}

		Collection<Method> methods = uniqueMethods.values();
		return methods.toArray(new Method[0]);
	}

	private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
		for (Method currentMethod : methods) {
			if (!currentMethod.isBridge()) {
				String signature = getSignature(currentMethod);
				if (!uniqueMethods.containsKey(signature)) {
					uniqueMethods.put(signature, currentMethod);
				}
			}
		}
	}

	private String getSignature(Method method) {
		StringBuilder sb = new StringBuilder();
		Class<?> returnType = method.getReturnType();
		if (returnType != null) {
			sb.append(returnType.getName()).append('#');
		}
		sb.append(method.getName());
		Class<?>[] parameters = method.getParameterTypes();
		for (int i = 0; i < parameters.length; i++) {
			sb.append(i == 0 ? ':' : ',').append(parameters[i].getName());
		}
		return sb.toString();
	}

	public static boolean canControlMemberAccessible() {
		try {
			SecurityManager securityManager = System.getSecurityManager();
			if (securityManager != null) {
				securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
			}
		}
		catch (SecurityException e) {
			return false;
		}
		return true;
	}

	public Class<?> getType() {
		return type;
	}

	public Constructor<?> getDefaultConstructor() {
		if (defaultConstructor == null) {
			throw new ReflectionException("There is no default constructor for " + type);
		}
		return defaultConstructor;
	}

	public boolean hasDefaultConstructor() {
		return defaultConstructor != null;
	}

	public Invoker getSetInvoker(String propertyName) {
		Invoker method = setMethods.get(propertyName);
		if (method == null) {
			throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
		}
		return method;
	}

	public Invoker getGetInvoker(String propertyName) {
		Invoker method = getMethods.get(propertyName);
		if (method == null) {
			throw new ReflectionException("There is not getter for property named '" + propertyName + "' in '" + type + "'");
		}
		return method;
	}

	public Class<?> getSetterType(String propertyName) {
		Class<?> clazz = setTypes.get(propertyName);
		if (clazz == null) {
			throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
		}
		return clazz;
	}

	public Class<?> getGetterType(String propertyName) {
		Class<?> clazz = getTypes.get(propertyName);
		if (clazz == null) {
			throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
		}
		return clazz;
	}

	public String[] getGetablePropertyNames() {
		return readablePropertyNames;
	}

	public String[] getSetablePropertyNames() {
		return writablePropertyNames;
	}

	public boolean hasSetter(String properName) {
		return setMethods.containsKey(properName);
	}

	public boolean hasGetter(String propertyName) {
		return getMethods.containsKey(propertyName);
	}

	public String findPropertyName(String name) {
		return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
	}

	private static boolean isRecord(Class<?> clazz) {
		try {
			return isRecordMethodHandle != null && (boolean) isRecordMethodHandle.invokeExact(clazz);
		}
		catch (Throwable e) {
			throw new ReflectionException("Failed to invoke 'Class.isRecord()'.", e);
		}
	}

	private static MethodHandle getIsRecordMethodHandle() {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType mt = MethodType.methodType(boolean.class);
		try {
			return lookup.findVirtual(Class.class, "isRecord", mt);
		}
		catch (NoSuchMethodException | IllegalAccessException e) {
			return null;
		}
	}
}
