package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class ChooseSqlNode implements SqlNode {

	private final SqlNode defaultSqlNode;
	private final List<SqlNode> ifSqlNodes;

	public ChooseSqlNode(List<SqlNode> ifSqlNodes, SqlNode defaultSqlNode) {
		this.ifSqlNodes = ifSqlNodes;
		this.defaultSqlNode = defaultSqlNode;
	}

	@Override
	public boolean apply(DynamicContext context) {
		for (SqlNode sqlNode : ifSqlNodes) {
			if (sqlNode.apply(context)) {
				return true;
			}
		}

		if (defaultSqlNode != null) {
			defaultSqlNode.apply(context);
			return true;
		}
		return false;
	}
}
