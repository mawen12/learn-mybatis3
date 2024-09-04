package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.builder.BaseBuilder;
import com.mawen.learn.mybatis.builder.BuilderException;
import com.mawen.learn.mybatis.parsing.XNode;
import com.mawen.learn.mybatis.session.Configuration;

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

	private interface NodeHandler {

		void handleNode(XNode nodeToHandle, List<SqlNode> targetContents);
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
