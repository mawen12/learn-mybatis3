package com.mawen.learn.mybatis.builder.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mawen.learn.mybatis.annotations.Delete;
import com.mawen.learn.mybatis.annotations.DeleteProvider;
import com.mawen.learn.mybatis.annotations.Insert;
import com.mawen.learn.mybatis.annotations.InsertProvider;
import com.mawen.learn.mybatis.annotations.Options;
import com.mawen.learn.mybatis.annotations.Select;
import com.mawen.learn.mybatis.annotations.SelectKey;
import com.mawen.learn.mybatis.annotations.SelectProvider;
import com.mawen.learn.mybatis.annotations.Update;
import com.mawen.learn.mybatis.annotations.UpdateProvider;
import com.mawen.learn.mybatis.builder.MapperBuilderAssistant;
import com.mawen.learn.mybatis.mapping.SqlCommandType;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class MapperAnnotationBuilder {

	private static final Set<Class<? extends Annotation>> statementAnnotationTypes = Stream
			.of(Select.class, Update.class, Insert.class, Delete.class,
					SelectProvider.class, UpdateProvider.class, InsertProvider.class, DeleteProvider.class)
			.collect(Collectors.toSet());

	private final Configuration configuration;
	private final MapperBuilderAssistant assistant;
	private final Class<?> type;

	public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
		String resource = type.getName().replace('.','/') + ".java (best guess)";
		this.assistant = new MapperBuilderAssistant(configuration, resource);
		this.configuration = configuration;
		this.type = type;
	}

	private class AnnotationWrapper {

		private final Annotation annotation;
		private final String databaseId;
		private final SqlCommandType sqlCommandType;

		AnnotationWrapper(Annotation annotation) {
			super();
			this.annotation = annotation;

			if (annotation instanceof Select) {
				databaseId = ((Select) annotation).databaseId();
				sqlCommandType = SqlCommandType.SELECT;
			}
			else if (annotation instanceof Update) {
				databaseId = ((Update) annotation).databaseId();
				sqlCommandType = SqlCommandType.UPDATE;
			}
			else if (annotation instanceof Insert) {
				databaseId = ((Insert) annotation).databaseId();
				sqlCommandType = SqlCommandType.INSERT;
			}
			else if (annotation instanceof Delete) {
				databaseId = ((Delete) annotation).databaseId();
				sqlCommandType = SqlCommandType.DELETE;
			}
			else if (annotation instanceof SelectProvider) {
				databaseId = ((SelectProvider) annotation).databaseId();
				sqlCommandType = SqlCommandType.SELECT;
			}
			else if (annotation instanceof UpdateProvider) {
				databaseId = ((UpdateProvider) annotation).databaseId();
				sqlCommandType = SqlCommandType.UPDATE;
			}
			else if (annotation instanceof InsertProvider) {
				databaseId = ((InsertProvider) annotation).databaseId();
				sqlCommandType = SqlCommandType.INSERT;
			}
			else if (annotation instanceof DeleteProvider) {
				databaseId = ((DeleteProvider) annotation).databaseId();
				sqlCommandType = SqlCommandType.DELETE;
			}
			else {
				sqlCommandType = SqlCommandType.UNKNOWN;
				if (annotation instanceof Options) {
					databaseId = ((Options) annotation).databaseId();
				}
				else if (annotation instanceof SelectKey) {
					databaseId = ((SelectKey) annotation).databaseId();
				}
				else {
					databaseId = "";
				}
			}
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public MapperBuilderAssistant getAssistant() {
		return assistant;
	}

	public Class<?> getType() {
		return type;
	}
}
