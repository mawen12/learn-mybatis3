package com.mawen.learn.mybatis.builder;

import java.util.List;

import com.mawen.learn.mybatis.mapping.Discriminator;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.mapping.ResultMapping;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class ResultMapResolver {

	private final MapperBuilderAssistant assistant;
	private final String id;
	private final Class<?> type;
	private final String extend;
	private final Discriminator discriminator;
	private final List<ResultMapping> resultMappings;
	private final Boolean autoMapping;

	public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, String extend, Discriminator discriminator, List<ResultMapping> resultMappings, Boolean autoMapping) {
		this.assistant = assistant;
		this.id = id;
		this.type = type;
		this.extend = extend;
		this.discriminator = discriminator;
		this.resultMappings = resultMappings;
		this.autoMapping = autoMapping;
	}

	public ResultMap resolve() {
		return assistant.addResultMap(id, type, extend, discriminator, resultMappings, autoMapping);
	}
}
