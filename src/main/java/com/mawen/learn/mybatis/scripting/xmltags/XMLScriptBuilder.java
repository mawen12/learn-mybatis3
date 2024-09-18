package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.builder.BaseBuilder;
import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.mapping.SqlSource;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.scripting.defaults.RawSqlSource;
import com.mawen.learn.mybatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class XMLScriptBuilder extends BaseBuilder {

	private final XNode context;
	private boolean isDynamic;
	private final Class<?> parameterType;
	private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

	public XMLScriptBuilder(Configuration configuration, XNode context) {
		this(configuration, context, null);
	}

	public XMLScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType) {
		super(configuration);
		this.context = context;
		this.parameterType = parameterType;
		initNodeHandlerMap();
	}

	private void initNodeHandlerMap() {
		nodeHandlerMap.put("trim", new TrimHandler());
		nodeHandlerMap.put("where", new WhereHandler());
		nodeHandlerMap.put("set", new SetHandler());
		nodeHandlerMap.put("foreach", new ForEachHandler());
		nodeHandlerMap.put("if", new IfHandler());
		nodeHandlerMap.put("choose", new ChooseHandler());
		nodeHandlerMap.put("when", new IfHandler());
		nodeHandlerMap.put("otherwise", new OtherwiseHandler());
		nodeHandlerMap.put("bind", new BindHandler());
	}

	public SqlSource parseScriptNode() {
		MixedSqlNode rootSqlNode = parseDynamicTags(context);
		SqlSource sqlSource;
		if (isDynamic) {
			sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
		}
		else {
			sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
		}
		return sqlSource;
	}

	protected MixedSqlNode parseDynamicTags(XNode node) {
		List<SqlNode> contents = new ArrayList<>();
		NodeList children = node.getNode().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			XNode child = node.newXNode(children.item(i));
			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
				String data = child.getStringBody("");
				TextSqlNode textSqlNode = new TextSqlNode(data);
				if (textSqlNode.isDynamic()) {
					contents.add(textSqlNode);
					isDynamic = true;
				}
				else {
					contents.add(new StaticTextSqlNode(data));
				}
			}
			else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = child.getNode().getNodeName();
				NodeHandler handler = nodeHandlerMap.get(nodeName);
				if (handler == null) {
					throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
				}
				handler.handleNode(child, contents);
				isDynamic = true;
			}
		}
		return new MixedSqlNode(contents);
	}

	private interface NodeHandler {

		void handleNode(XNode nodeToHandle, List<SqlNode> targetContents);
	}

	private class BindHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			String name = nodeToHandle.getStringAttribute("name");
			String expression = nodeToHandle.getStringAttribute("value");
			VarDeclSqlNode varDeclSqlNode = new VarDeclSqlNode(name, expression);
			targetContents.add(varDeclSqlNode);
		}
	}

	private class TrimHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			String prefix = nodeToHandle.getStringAttribute("prefix");
			String prefixOverrides = nodeToHandle.getStringAttribute("prefixOverrides");
			String suffix = nodeToHandle.getStringAttribute("suffix");
			String suffixOverrides = nodeToHandle.getStringAttribute("suffixOverrides");
			TrimSqlNode trimSqlNode = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
			targetContents.add(trimSqlNode);
		}
	}

	private class WhereHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			WhereSqlNode whereSqlNode = new WhereSqlNode(configuration, mixedSqlNode);
			targetContents.add(whereSqlNode);
		}
	}

	private class SetHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			SetSqlNode setSqlNode = new SetSqlNode(configuration, mixedSqlNode);
			targetContents.add(setSqlNode);
		}
	}

	private class ForEachHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			String collection = nodeToHandle.getStringAttribute("collection");
			Boolean nullable = nodeToHandle.getBooleanAttribute("nullable");
			String item = nodeToHandle.getStringAttribute("item");
			String index = nodeToHandle.getStringAttribute("index");
			String open = nodeToHandle.getStringAttribute("open");
			String close = nodeToHandle.getStringAttribute("close");
			String separator = nodeToHandle.getStringAttribute("separator");
			ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration, mixedSqlNode, collection, nullable, index, item, open, close, separator);
			targetContents.add(forEachSqlNode);
		}
	}

	private class IfHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			String test = nodeToHandle.getStringAttribute("test");
			IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
			targetContents.add(ifSqlNode);
		}
	}

	private class OtherwiseHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
			String test = nodeToHandle.getStringAttribute("test");
			IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
			targetContents.add(ifSqlNode);
		}
	}

	private class ChooseHandler implements NodeHandler {

		@Override
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> whenSqlNodes = new ArrayList<>();
		}

		private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<SqlNode> ifSqlNodes, List<SqlNode> defaultSqlNodes) {
			List<XNode> children = chooseSqlNode.getChildren();
			for (XNode child : children) {
				String nodeName = child.getNode().getNodeName();
				NodeHandler handler = nodeHandlerMap.get(nodeName);
				if (handler instanceof IfHandler) {
					handler.handleNode(child, ifSqlNodes);
				}
				else if (handler instanceof OtherwiseHandler) {
					handler.handleNode(child, defaultSqlNodes);
				}
			}
		}

		private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
			SqlNode defaultSqlNode = null;
			if (defaultSqlNodes.size() == 1) {
				defaultSqlNode = defaultSqlNodes.get(0);
			}
			else if (defaultSqlNodes.size() > 1) {
				throw new BuilderException("Too many default (otherwise) elements in choose statement.");
			}
			return defaultSqlNode;
		}

	}
}
