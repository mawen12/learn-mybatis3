package com.mawen.learn.mybatis.builder.xml;

import java.util.Map;

import com.mawen.learn.mybatis.builder.BaseBuilder;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.parsing.XPathParser;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class XMLMapperBuilder extends BaseBuilder {

	private final XPathParser parser;
	private final MapperBuilderAssistant builderAssistant;
	private final Map<String, XNode> sqlFragments;
	private final String resource;


}
