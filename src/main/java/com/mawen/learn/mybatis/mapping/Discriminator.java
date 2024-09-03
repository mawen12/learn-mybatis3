package com.mawen.learn.mybatis.mapping;

import java.util.Collections;
import java.util.Map;

import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class Discriminator {

	private ResultMapping resultMapping;
	private Map<String, String> discriminatorMap;

	Discriminator() {}

	public ResultMapping getResultMapping() {
		return resultMapping;
	}

	public Map<String, String> getDiscriminatorMap() {
		return discriminatorMap;
	}

	public String getMapIdFor(String s) {
		return discriminatorMap.get(s);
	}

	public static class Builder {

		private Discriminator discriminator = new Discriminator();

		public Builder(Configuration configuration, ResultMapping resultMapping, Map<String, String> discriminatorMap) {
			discriminator.resultMapping = resultMapping;
			discriminator.discriminatorMap = discriminatorMap;
		}

		public Discriminator build() {
			assert  discriminator.resultMapping != null;
			assert discriminator.discriminatorMap != null;
			assert !discriminator.discriminatorMap.isEmpty();

			discriminator.discriminatorMap = Collections.unmodifiableMap(discriminator.discriminatorMap);
			return discriminator;
		}
	}
}
