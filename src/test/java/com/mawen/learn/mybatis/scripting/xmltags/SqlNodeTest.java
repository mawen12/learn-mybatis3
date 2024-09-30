package com.mawen.learn.mybatis.scripting.xmltags;

import com.mawen.learn.mybatis.session.Configuration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
abstract class SqlNodeTest {

	@Mock
	protected Configuration configuration;

	@Mock
	protected DynamicContext context;

	public abstract void shouldApply() throws Exception;
}