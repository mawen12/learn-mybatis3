package com.mawen.learn.mybatis.builder;

import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.type.TypeAliasRegistry;
import com.mawen.learn.mybatis.type.TypeHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MapperBuilderAssistantTest {

	@Mock
	private Configuration configuration;

	@Mock
	TypeAliasRegistry typeAliasRegistry;

	@Mock
	private TypeHandlerRegistry typeHandlerRegistry;

}