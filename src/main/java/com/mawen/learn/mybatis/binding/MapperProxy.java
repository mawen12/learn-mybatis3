package com.mawen.learn.mybatis.binding;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.mawen.learn.mybatis.reflection.ExceptionUtil;
import com.mawen.learn.mybatis.session.SqlSession;
import com.mawen.learn.mybatis.util.MapUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 6378746305736357525L;

    private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
            | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PUBLIC;
    private static final Constructor<MethodHandles.Lookup> lookupConstructor;
    private static final Method privateLookupInMethod;

    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethodInvoker> methodCache;

    static {
        Method privateLookupIn;
        try {
            privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            privateLookupIn = null;
        }
        privateLookupInMethod = privateLookupIn;

        Constructor<MethodHandles.Lookup> lookup = null;
        if (privateLookupInMethod == null) {
            try {
                lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookup.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("There is neither 'privateLookupIn(Class, Lookup)' nor 'lookup(Class, int)' method in java.lang.invoke.MethodHandles.", e);
            } catch (Exception e) {
                lookup = null;
            }
        }

        lookupConstructor = lookup;
    }

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethodInvoker> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else {
                return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    private MapperMethodInvoker cachedInvoker(Method method) throws Throwable {
        try {
            return MapUtil.computIfAbsent(methodCache, method, m -> {
                if (m.isDefault()) {
                    try {
                        if (privateLookupInMethod == null) {
                            return new DefaultMethodInvoker(getMethodHandleJava8(method));
                        } else {
                            return new DefaultMethodInvoker(getMethodHandleJava9(method));
                        }
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return new PlainMethodInvoker(new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
                }
            });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            throw cause == null ? e : cause;
        }
    }

    private MethodHandle getMethodHandleJava9(Method method) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> declaringClass = method.getDeclaringClass();
        return ((MethodHandles.Lookup) privateLookupInMethod.invoke(null, declaringClass, MethodHandles.lookup()))
                .findSpecial(declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass);
    }

    private MethodHandle getMethodHandleJava8(Method method) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> declaringClass = method.getDeclaringClass();
        return lookupConstructor.newInstance(declaringClass, ALLOWED_MODES).unreflectSpecial(method, declaringClass);
    }

    interface MapperMethodInvoker {
        Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
    }

    private static class PlainMethodInvoker implements MapperMethodInvoker {

        private final MapperMethod mapperMethod;

        public PlainMethodInvoker(MapperMethod mapperMethod) {
            super();
            this.mapperMethod = mapperMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
            return mapperMethod.execute(sqlSession, args);
        }
    }

    private static class DefaultMethodInvoker implements MapperMethodInvoker {

        private final MethodHandle methodHandle;

        public DefaultMethodInvoker(MethodHandle methodHandle) {
            super();
            this.methodHandle = methodHandle;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
            return methodHandle.bindTo(proxy).invokeWithArguments(args);
        }
    }
}
