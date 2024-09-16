package com.mawen.learn.mybatis.builder.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.builder.IncompleteElementException;
import com.mawen.learn.mybatis.builder.MapperBuilderAssistant;
import com.mawen.learn.mybatis.parsing.PropertyParser;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.session.Configuration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/16
 */
public class XMLIncludeTransformer {

	private final Configuration configuration;
	private final MapperBuilderAssistant builderAssistant;

	public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
		this.configuration = configuration;
		this.builderAssistant = builderAssistant;
	}

	public void applyIncludes(Node source) {
		Properties variablesContext = new Properties();
		Properties configurationVariables = configuration.getVariables();
		Optional.ofNullable(configurationVariables).ifPresent(variablesContext::putAll);
		applyIncludes(source, variablesContext, false);
	}

	private void applyIncludes(Node source, Properties variablesContext, boolean included) {
		if ("include".equals(source.getNodeName())) {
			Node toInclude = findSqlFragment(getStringAttribute(source, "refid"), variablesContext);
			Properties toIncludeContext = getVariablesContext(source, variablesContext);
			applyIncludes(toInclude, toIncludeContext, included);
			if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
				toInclude = source.getOwnerDocument().importNode(toInclude, true);
			}

			source.getParentNode().replaceChild(toInclude, source);
			while (toInclude.hasChildNodes()) {
				toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
			}
			toInclude.getParentNode().removeChild(toInclude);
		}
		else if (source.getNodeType() == Node.ELEMENT_NODE) {
			if (included && !variablesContext.isEmpty()) {
				NamedNodeMap attributes = source.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attr = attributes.item(i);
					attr.setNodeValue(PropertyParser.parse(attr.getNodeValue(), variablesContext));
				}
			}

			NodeList children = source.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				applyIncludes(children.item(i), variablesContext, included);
			}
		}
		else if (included && (source.getNodeType() == Node.TEXT_NODE || source.getNodeType() == Node.CDATA_SECTION_NODE) && !variablesContext.isEmpty()) {
			source.setNodeValue(PropertyParser.parse(source.getNodeValue(), variablesContext));
		}
	}

	private Node findSqlFragment(String refid, Properties variables) {
		refid = PropertyParser.parse(refid, variables);
		refid = builderAssistant.applyCurrentNamespace(refid, true);

		try {
			XNode nodeToInclude = configuration.getSqlFragments().get(refid);
			return nodeToInclude.getNode().cloneNode(true);
		}
		catch (IllegalArgumentException e) {
			throw new IncompleteElementException("Could not find SQL statement to include with refId '" + refid + "'", e);
		}
	}

	private Properties getVariablesContext(Node node, Properties inheritedVariablesContext) {
		Map<String, String> declaredProperties = null;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				String name = getStringAttribute(n, "name");
				String value = PropertyParser.parse(getStringAttribute(n, "value"), inheritedVariablesContext);

				if (declaredProperties == null) {
					declaredProperties = new HashMap<>();
				}

				if (declaredProperties.put(name, value) != null) {
					throw new BuilderException("Variable " + name + " defined twice in the same include definition");
				}
			}
		}

		if (declaredProperties == null) {
			return inheritedVariablesContext;
		}
		else {
			Properties newProperties = new Properties();
			newProperties.putAll(declaredProperties);
			newProperties.putAll(inheritedVariablesContext);
			return newProperties;
		}
	}

	private String getStringAttribute(Node node, String name) {
		return node.getAttributes().getNamedItem(name).getNodeValue();
	}
}
