package com.mawen.learn.mybatis.builder.xml;

import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;

import com.mawen.learn.mybatis.builder.CachedAuthorMapper;
import com.mawen.learn.mybatis.builder.CustomLongTypeHandler;
import com.mawen.learn.mybatis.builder.CustomObjectWrapperFactory;
import com.mawen.learn.mybatis.builder.CustomReflectorFactory;
import com.mawen.learn.mybatis.builder.CustomStringTypeHandler;
import com.mawen.learn.mybatis.builder.ExampleObjectFactory;
import com.mawen.learn.mybatis.builder.ExamplePlugin;
import com.mawen.learn.mybatis.builder.mapper.CustomMapper;
import com.mawen.learn.mybatis.builder.typehandler.CustomIntegerTypeHandler;
import com.mawen.learn.mybatis.datasource.unpooled.UnpooledDataSource;
import com.mawen.learn.mybatis.domain.blog.Author;
import com.mawen.learn.mybatis.domain.blog.Blog;
import com.mawen.learn.mybatis.domain.blog.mappers.BlogMapper;
import com.mawen.learn.mybatis.domain.blog.mappers.NestedBlogMapper;
import com.mawen.learn.mybatis.domain.jpetstore.Cart;
import com.mawen.learn.mybatis.executor.loader.cglib.CglibProxyFactory;
import com.mawen.learn.mybatis.executor.loader.javassist.JavassistProxyFactory;
import com.mawen.learn.mybatis.io.JBoss6VFS;
import com.mawen.learn.mybatis.io.Resources;
import com.mawen.learn.mybatis.logging.slf4j.Slf4jImpl;
import com.mawen.learn.mybatis.mapping.Environment;
import com.mawen.learn.mybatis.scripting.defaults.RawLanguageDriver;
import com.mawen.learn.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.mawen.learn.mybatis.session.AutoMappingBehavior;
import com.mawen.learn.mybatis.session.AutoMappingUnknownColumnBehavior;
import com.mawen.learn.mybatis.session.Configuration;
import com.mawen.learn.mybatis.session.ExecutorType;
import com.mawen.learn.mybatis.session.LocalCacheScope;
import com.mawen.learn.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.mawen.learn.mybatis.type.EnumTypeHandler;
import com.mawen.learn.mybatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class XMLConfigBuilderTest {

	@Test
	void shouldSuccessfullyLoadMinimalXMLConfigFile() throws IOException {

		String resource = "com/mawen/learn/mybatis/builder/MinimalMapperConfig.xml";

		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
			Configuration config = builder.parse();

			assertNotNull(config);

			assertThat(config.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.PARTIAL);
			assertThat(config.getAutoMappingUnknownColumnBehavior()).isEqualByComparingTo(AutoMappingUnknownColumnBehavior.NONE);
			assertThat(config.isCacheEnabled()).isTrue();
			assertThat(config.getProxyFactory()).isInstanceOf(JavassistProxyFactory.class);
			assertThat(config.isLazyLoadingEnabled()).isFalse();
			assertThat(config.isAggressiveLazyLoading()).isFalse();
			assertThat(config.isMultipleResultSetsEnabled()).isTrue();
			assertThat(config.isUseColumnLabel()).isTrue();
			assertThat(config.isUseGeneratedKeys()).isFalse();
			assertThat(config.getDefaultExecutorType()).isEqualTo(ExecutorType.SIMPLE);
			assertNull(config.getDefaultStatementTimeout());
			assertNull(config.getDefaultResultSetType());
			assertThat(config.isMapUnderscoreToCamelCase()).isFalse();
			assertThat(config.isSafeRowBoundsEnabled()).isFalse();
			assertThat(config.getLocalCacheScope()).isEqualTo(LocalCacheScope.SESSION);
			assertThat(config.getJdbcTypeForNull()).isEqualTo(JdbcType.OTHER);
			assertThat(config.getLazyLoadTriggerMethods()).isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")));
			assertThat(config.isSafeResultHandlerEnabled()).isTrue();
			assertThat(config.getDefaultScriptingLanguageInstance()).isInstanceOf(XMLLanguageDriver.class);
			assertThat(config.isCallSettersOnNulls()).isFalse();
			assertNull(config.getLogPrefix());
			assertNull(config.getLogImpl());
			assertNull(config.getConfigurationFactory());
			assertThat(config.getTypeHandlerRegistry().getTypeHandler(RoundingMode.class)).isInstanceOf(EnumTypeHandler.class);
			assertThat(config.isShrinkWhitespacesInSql()).isFalse();
			assertThat(config.isArgNameBasedConstructorAutoMapping()).isFalse();
			assertThat(config.getDefaultSqlProviderType()).isNull();
			assertThat(config.isNullableOnForEach()).isFalse();
		}
	}

	@Test
	void shouldSuccessfullyLoadXMLConfigFile() {

		String resource = "com/mawen/learn/mybatis/builder/CustomizedSettingsMapperConfig.xml";

		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
			Configuration config = builder.parse();

			assertEquals(AutoMappingBehavior.NONE, config.getAutoMappingBehavior());
			assertEquals(AutoMappingUnknownColumnBehavior.WARNING, config.getAutoMappingUnknownColumnBehavior());
			assertFalse(config.isCacheEnabled());
			assertTrue(config.getProxyFactory() instanceof CglibProxyFactory);
			assertTrue(config.isLazyLoadingEnabled());
			assertTrue(config.isAggressiveLazyLoading());
			assertFalse(config.isUseColumnLabel());
			assertTrue(config.isUseGeneratedKeys());
			assertEquals(ExecutorType.BATCH, config.getDefaultExecutorType());
			assertEquals(Integer.valueOf(10), config.getDefaultStatementTimeout());
			assertEquals(Integer.valueOf(100), config.getDefaultFetchSize());
			assertTrue(config.isMapUnderscoreToCamelCase());
			assertTrue(config.isSafeRowBoundsEnabled());
			assertEquals(LocalCacheScope.STATEMENT, config.getLocalCacheScope());
			assertEquals(JdbcType.NULL, config.getJdbcTypeForNull());
			assertEquals(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString", "xxx")), config.getLazyLoadTriggerMethods());
			assertFalse(config.isSafeResultHandlerEnabled());
			assertTrue(config.getDefaultScriptingLanguageInstance() instanceof RawLanguageDriver);
			assertTrue(config.isCallSettersOnNulls());
			assertEquals("mybatis_", config.getLogPrefix());
			assertEquals(Slf4jImpl.class.getName(), config.getLogImpl().getName());
			assertEquals(JBoss6VFS.class.getName(), config.getVfsImpl().getName());
			assertEquals(String.class.getName(), config.getConfigurationFactory().getName());
			assertTrue(config.isShrinkWhitespacesInSql());
			assertTrue(config.isArgNameBasedConstructorAutoMapping());

			assertEquals(Author.class,config.getTypeAliasRegistry().getTypeAliases().get("blogauthor"));
			assertEquals(Blog.class, config.getTypeAliasRegistry().getTypeAliases().get("blog"));
			assertEquals(Cart.class, config.getTypeAliasRegistry().getTypeAliases().get("cart"));

			assertTrue(config.getTypeHandlerRegistry().getTypeHandler(Integer.class) instanceof CustomIntegerTypeHandler);
			assertTrue(config.getTypeHandlerRegistry().getTypeHandler(Long.class) instanceof CustomLongTypeHandler);
			assertTrue(config.getTypeHandlerRegistry().getTypeHandler(String.class) instanceof CustomStringTypeHandler);
			assertTrue(config.getTypeHandlerRegistry().getTypeHandler(String.class, JdbcType.VARCHAR) instanceof CustomStringTypeHandler);

			ExampleObjectFactory objectFactory = (ExampleObjectFactory) config.getObjectFactory();
			assertEquals(1, objectFactory.getProperties().size());
			assertEquals("100",objectFactory.getProperties().getProperty("objectFactoryProperty"));

			assertTrue(config.getObjectWrapperFactory() instanceof CustomObjectWrapperFactory);

			assertTrue(config.getReflectorFactory() instanceof CustomReflectorFactory);

			ExamplePlugin plugin = (ExamplePlugin) config.getInterceptors().get(0);
			assertEquals(1, plugin.getProperties().size());
			assertEquals("100", plugin.getProperties().getProperty("pluginProperty"));

			Environment environment = config.getEnvironment();
			assertEquals("development", environment.getId());
			assertTrue(environment.getDataSource() instanceof UnpooledDataSource);
			assertTrue(environment.getTransactionFactory() instanceof JdbcTransactionFactory);

			assertEquals("derby", config.getDatabaseId());

			assertEquals(4, config.getMapperRegistry().getMappers().size());
			assertTrue(config.getMapperRegistry().hasMapper(CachedAuthorMapper.class));
			assertTrue(config.getMapperRegistry().hasMapper(CustomMapper.class));
			assertTrue(config.getMapperRegistry().hasMapper(BlogMapper.class));
			assertTrue(config.getMapperRegistry().hasMapper(NestedBlogMapper.class));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static class MySqlProvider {

		public static String providerSql() {
			return "SELECT 1";
		}

		private MySqlProvider() {}
	}
}