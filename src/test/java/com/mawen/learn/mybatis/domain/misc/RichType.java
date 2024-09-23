package com.mawen.learn.mybatis.domain.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/23
 */
public class RichType {

	private RichType richType;

	private String richField;

	private String richProperty;

	private Map richMap = new HashMap<>();

	private List richList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("bar");
		}
	};

	public RichType getRichType() {
		return richType;
	}

	public void setRichType(RichType richType) {
		this.richType = richType;
	}

	public String getRichProperty() {
		return richProperty;
	}

	public void setRichProperty(String richProperty) {
		this.richProperty = richProperty;
	}

	public List getRichList() {
		return richList;
	}

	public void setRichList(List richList) {
		this.richList = richList;
	}

	public Map getRichMap() {
		return richMap;
	}

	public void setRichMap(Map richMap) {
		this.richMap = richMap;
	}
}
