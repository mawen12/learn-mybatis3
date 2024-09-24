package com.mawen.learn.mybatis.builder.xml;

import java.io.IOException;
import java.io.InputStream;

import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.session.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XMLMapperBuilderTest {

	@Test
	void shouldSuccessfullyLoadXMLMapperFile() throws IOException {

		Configuration configuration = new Configuration();

		String resource = "com/mawen/learn/mybatis/builder/AuthorMapper.xml";

		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
			builder.parse();
		}
	}
}