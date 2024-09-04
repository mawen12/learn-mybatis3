package com.mawen.learn.mybatis.scripting.xmltags;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public interface SqlNode {

	boolean apply(DynamicContext context);
}
