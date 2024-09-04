package com.mawen.learn.mybatis.reflection.property;

import java.util.Iterator;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {

	private String name;
	private final String indexedName;
	private String index;
	private final String children;

	public PropertyTokenizer(String fullname) {
		int delim = fullname.indexOf('.');
		if (delim > -1) {
			name = fullname.substring(0, delim);
			children = fullname.substring(delim + 1);
		}
		else {
			name = fullname;
			children = null;
		}

		indexedName = name;
		delim = name.indexOf('[');
		if (delim > -1) {
			index = name.substring(delim + 1, name.length() - 1);
			name = name.substring(0, delim);
		}
	}

	public String getName() {
		return name;
	}

	public String getIndexedName() {
		return indexedName;
	}

	public String getIndex() {
		return index;
	}

	public String getChildren() {
		return children;
	}

	@Override
	public boolean hasNext() {
		return children != null;
	}

	@Override
	public PropertyTokenizer next() {
		return new PropertyTokenizer(children);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove is not supported, as it has to meaning in the context of properties.");
	}
}