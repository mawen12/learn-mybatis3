package com.mawen.learn.mybatis.scripting.xmltags;

import com.mawen.learn.mybatis.io.Resources;
import ognl.DefaultClassResolver;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class OgnlClassResolver extends DefaultClassResolver {

	@Override
	protected Class toClassForName(String className) throws ClassNotFoundException {
		return Resources.classForName(className);
	}
}
