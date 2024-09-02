package com.mawen.learn.mybatis.binding;

import java.lang.reflect.Method;

import com.mawen.learn.mybatis.mapping.MappedStatement;
import com.mawen.learn.mybatis.mapping.SqlCommandType;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/1
 */
public class MappedMethod {

	private final SqlCommand command;
	private final MethodSignature signature;


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
					throw new BindException("Invalid bound statement (not found): " + mapperInterface.getName() + "." + methodName);
				}
			}
			else {
				name = ms.getId();
			}
		}
	}
}
