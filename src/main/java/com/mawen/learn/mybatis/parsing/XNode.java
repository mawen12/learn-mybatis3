package com.mawen.learn.mybatis.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class XNode {

	private final Node node;
	private final String name;
	private final String body;
	private final Properties attributes;
	private final Properties variables;
	private final XPathParser xpathParser;

	public XNode(XPathParser xpathParser, Node node, Properties variables) {
		this.node = node;
		this.variables = variables;
		this.xpathParser = xpathParser;
		this.name = node.getNodeName();
		this.attributes = parseAttributes(node);
		this.body = parseBody(node);
	}

	public XNode newXNode(Node node) {
		return new XNode(xpathParser, node, variables);
	}

	public XNode getParent() {
		Node parent = node.getParentNode();
		if (!(parent instanceof Element)) {
			return null;
		}
		else {
			return new XNode(xpathParser, parent, variables);
		}
	}

	public String getPath() {
		StringBuilder sb = new StringBuilder();
		Node current = node;
		while (current instanceof Element) {
			if (current != node) {
				sb.insert(0, "/");
			}
			sb.insert(0, current.getNodeName());
			current = current.getParentNode();
		}
		return sb.toString();
	}

	public String getValueBasedIdentifier() {
		StringBuilder sb = new StringBuilder();
		XNode current = this;
		while (current != null) {
			if (current != this) {
				sb.insert(0, "_");
			}

			String value = current.getStringAttribute("id", current.getStringAttribute("value", current.getStringAttribute("property", (String) null)));
			if (value != null) {
				value = value.replace('.', '_');
				sb.insert(0, ']');
				sb.insert(0, value);
				sb.insert(0, '[');
			}

			sb.insert(0, current.getName());
			current = current.getParent();
		}
		return sb.toString();
	}

	public String evalString(String expression) {
		return xpathParser.evalString(node, expression);
	}

	public Boolean evalBoolean(String expression) {
		return xpathParser.evalBoolean(node, expression);
	}

	public Double evalDouble(String expression) {
		return xpathParser.evalDouble(node, expression);
	}

	public List<XNode> evalNodes(String expression) {
		return xpathParser.evalNodes(node, expression);
	}

	public XNode evalNode(String expression) {
		return xpathParser.evalNode(node, expression);
	}

	public Node getNode() {
		return node;
	}

	public String getName() {
		return name;
	}

	public String getStringBody() {
		return getStringBody(null);
	}

	public String getStringBody(String def) {
		return body == null ? def : body;
	}

	public Boolean getBooleanBody() {
		return getBooleanBody(null);
	}

	public Boolean getBooleanBody(Boolean def) {
		return body == null ? def : Boolean.valueOf(body);
	}

	public Integer getIntegerBody() {
		return getIntegerBody(null);
	}

	public Integer getIntegerBody(Integer def) {
		return body == null ? def : Integer.valueOf(body);
	}

	public Long getLongBody() {
		return getLongBody(null);
	}

	public Long getLongBody(Long ref) {
		return body == null ? ref : Long.valueOf(body);
	}

	public Double getDoubleBody() {
		return getDoubleBody(null);
	}

	public Double getDoubleBody(Double ref) {
		return body == null ? ref : Double.valueOf(body);
	}

	public Float getFloatBody() {
		return getFloatBody(null);
	}

	public Float getFloatBody(Float def) {
		return body == null ? def : Float.valueOf(body);
	}

	public <T extends Enum<T>> T getEnumAttribute(Class<T> enumType, String name) {
		return getEnumAttribute(enumType, name, null);
	}

	public <T extends Enum<T>> T getEnumAttribute(Class<T> enumType, String name, T def) {
		String value = getStringAttribute(name);
		return value == null ? def : Enum.valueOf(enumType, value);
	}

	public String getStringAttribute(String name, Supplier<String> defSupplier) {
		return getStringAttribute(name, defSupplier.get());
	}

	public String getStringAttribute(String name) {
		return getStringAttribute(name, (String) null);
	}

	public String getStringAttribute(String name, String def) {
		String value = attributes.getProperty(name);
		return value == null ? def : value;
	}

	public Boolean getBooleanAttribute(String name) {
		return getBooleanAttribute(name, null);
	}

	public Boolean getBooleanAttribute(String name, Boolean def) {
		String value = attributes.getProperty(name);
		return value == null ? def : Boolean.valueOf(value);
	}

	public Integer getIntegerAttribute(String name) {
		return getIntegerAttribute(name, null);
	}

	public Integer getIntegerAttribute(String name, Integer def) {
		String value = attributes.getProperty(name);
		return value == null ? def : Integer.valueOf(value);
	}

	public Long getLongAttribute(String name) {
		return getLongAttribute(name, null);
	}

	public Long getLongAttribute(String name, Long def) {
		String value = attributes.getProperty(name);
		return value == null ? def : Long.valueOf(value);
	}

	public Double getDoubleAttribute(String name) {
		return getDoubleAttribute(name, null);
	}

	public Double getDoubleAttribute(String name, Double ref) {
		String value = attributes.getProperty(name);
		return value == null ? ref : Double.valueOf(value);
	}

	public Float getFloatAttribute(String name) {
		return getFloatAttribute(name, null);
	}

	public Float getFloatAttribute(String name, Float ref) {
		String value = attributes.getProperty(name);
		return value == null ? ref : Float.valueOf(value);
	}

	public List<XNode> getChildren() {
		List<XNode> children = new ArrayList<>();
		NodeList nodeList = node.getChildNodes();
		if (nodeList != null) {
			for (int i = 0, n = nodeList.getLength(); i < n; i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					children.add(new XNode(xpathParser, node, variables));
				}
			}
		}
		return children;
	}

	public Properties getChildrenAsProperties() {
		Properties properties = new Properties();
		for (XNode child : getChildren()) {
			String name = child.getStringAttribute("name");
			String value = child.getStringAttribute("value");
			if (name != null && value != null) {
				properties.setProperty(name, value);
			}
		}
		return properties;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		toString(builder, 0);
		return builder.toString();
	}

	private void toString(StringBuilder builder, int level) {
		builder.append("<");
		builder.append(name);

		for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
			builder.append(" ");
			builder.append(entry.getKey());
			builder.append("=\"");
			builder.append(entry.getValue());
			builder.append("\"");
		}

		List<XNode> children = getChildren();
		if (!children.isEmpty()) {
			builder.append(">\n");
			for (XNode child : children) {
				indent(builder, level + 1);
				child.toString(builder, level + 1);
			}

			indent(builder, level);
			builder.append("</");
			builder.append(name);
			builder.append(">");
		}
		else if (body != null) {
			builder.append(">");
			builder.append(body);
			builder.append("</");
			builder.append(name);
			builder.append(">");
		}
		else {
			builder.append("/>");
			indent(builder, level);
		}
		builder.append("\n");
	}

	private void indent(StringBuilder builder, int level) {
		for (int i = 0; i < level; i++) {
			builder.append("    ");
		}
	}

	private Properties parseAttributes(Node node) {
		Properties attributes = new Properties();
		NamedNodeMap attributeNodes = node.getAttributes();
		if (attributes != null) {
			for (int i = 0; i < attributeNodes.getLength(); i++) {
				Node attribute = attributeNodes.item(i);
				String value = PropertyParser.parse(attribute.getNodeValue(), variables);
				attributes.put(attribute.getNodeName(), value);
			}
		}
		return attributes;
	}

	private String parseBody(Node node) {
		String data = getBodyData(node);
		if (data == null) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				data = getBodyData(child);
				if (data != null) {
					break;
				}
			}
		}
		return data;
	}

	private String getBodyData(Node child) {
		if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE) {
			String data = ((CharacterData) child).getData();
			return PropertyParser.parse(data, variables);
		}
		return null;
	}
}
