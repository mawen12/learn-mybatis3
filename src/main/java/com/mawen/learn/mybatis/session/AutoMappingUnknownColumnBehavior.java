package com.mawen.learn.mybatis.session;

import javax.management.StringValueExp;

import com.mawen.learn.mybatis.logging.Log;
import com.mawen.learn.mybatis.logging.LogFactory;
import com.mawen.learn.mybatis.mapping.MappedStatement;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public enum AutoMappingUnknownColumnBehavior {

	NONE {
		@Override
		public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {

		}
	},

	WARNING {
		@Override
		public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
			LogHolder.log.warn(AutoMappingUnknownColumnBehavior.buildMessage(mappedStatement, columnName, property, propertyType));
		}
	},

	FAILING {
		@Override
		public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
			throw new SqlSessionException(AutoMappingUnknownColumnBehavior.buildMessage(mappedStatement, columnName, property, propertyType));
		}
	}
	;


	public abstract void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType);


	private static String buildMessage(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
		return new StringBuilder("Unknown column is detected on '")
				.append(mappedStatement.getId())
				.append("' auto-mapping. Mapping parameters are ")
				.append("[")
				.append("columnName=").append(columnName)
				.append(",").append("propertyName=").append(property)
				.append(",").append("propertyType=").append(propertyType != null ? propertyType.getName() : null)
				.append("]")
				.toString();
	}

	private static class LogHolder {
		private static final Log log = LogFactory.getLog(AutoMappingUnknownColumnBehavior.class);
	}
}
