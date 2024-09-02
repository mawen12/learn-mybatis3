package com.mawen.learn.mybatis.session;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public enum AutoMappingUnknownColumnBehavior {

	NONE {

	}

	public abstract void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType);

}
