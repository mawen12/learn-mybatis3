package com.mawen.learn.mybatis.type;

import java.io.Serializable;
import java.net.URI;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.lang.model.type.NoType;

import com.mawen.learn.mybatis.domain.misc.RichType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeHandlerRegistryTest {

	private TypeHandlerRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new TypeHandlerRegistry();
	}

	@Test
	void shouldRegisterAndRetrieveTypeHandler() {
		TypeHandler<String> stringTypeHandler = registry.getTypeHandler(String.class);
		registry.register(String.class, JdbcType.LONGVARCHAR, stringTypeHandler);
		assertEquals(stringTypeHandler, registry.getTypeHandler(String.class, JdbcType.LONGVARCHAR));

		assertTrue(registry.hasTypeHandler(String.class));
		assertFalse(registry.hasTypeHandler(RichType.class));
		assertTrue(registry.hasTypeHandler(String.class, JdbcType.LONGVARCHAR));
		assertTrue(registry.hasTypeHandler(String.class, JdbcType.INTEGER));
		assertTrue(registry.getUnknownTypeHandler() instanceof UnknownTypeHandler);
	}

	@Test
	void shouldRegisterAndRetrieveComplexTypeHandler() {
		TypeHandler<List<URI>> fakeHandler = new TypeHandler<>() {
			@Override
			public void setParameter(PreparedStatement ps, int i, List<URI> parameter, JdbcType jdbcType) throws SQLException {
				//
			}

			@Override
			public List<URI> getResult(ResultSet rs, String columnName) throws SQLException {
				return null;
			}

			@Override
			public List<URI> getResult(ResultSet rs, int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public List<URI> getResult(CallableStatement cs, int columnIndex) throws SQLException {
				return null;
			}
		};

		TypeReference<List<URI>> type = new TypeReference<>() {};

		registry.register(type, fakeHandler);

		assertSame(fakeHandler, registry.getTypeHandler(type));
	}

	@Test
	void shouldAutoRegisterAndRetrieveComplexTypeHandler() {
		TypeHandler<List<URI>> fakeHandler = new BaseTypeHandler<>() {
			@Override
			public void setNonNullParameter(PreparedStatement ps, int i, List<URI> parameter, JdbcType jdbcType) throws SQLException {

			}

			@Override
			public List<URI> getNullableResult(ResultSet rs, String columnName) throws SQLException {
				return null;
			}

			@Override
			public List<URI> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
				return null;
			}

			@Override
			public List<URI> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
				return null;
			}
		};

		registry.register(fakeHandler);

		assertSame(fakeHandler, registry.getTypeHandler(new TypeReference<List<URI>>() {}));
	}

	@Test
	void shouldBindHandlersToWrappersAndPrimitiveIndividually() {
		registry.register(Integer.class, DateTypeHandler.class);
		assertSame(IntegerTypeHandler.class, registry.getTypeHandler(int.class).getClass());

		registry.register(Integer.class, IntegerTypeHandler.class);
		registry.register(int.class, DateTypeHandler.class);
		assertSame(IntegerTypeHandler.class, registry.getTypeHandler(Integer.class).getClass());

		registry.register(Integer.class, IntegerTypeHandler.class);
	}

	@Test
	void shouldReturnHandlerForSuperClassIfRegistered() {
		class MyDate extends Date {
			private static final long serialVersionUID = 1L;
		}

		assertEquals(DateTypeHandler.class, registry.getTypeHandler(MyDate.class).getClass());
	}

	@Test
	void shouldReturnHandlerForSuperSuperClassIfRegistered() {
		class MyDate1 extends Date {
			private static final long serialVersionUID = 1L;
		}
		class MyDate2 extends MyDate1 {
			private static final long serialVersionUID = 1L;
		}

		assertEquals(DateTypeHandler.class, registry.getTypeHandler(MyDate2.class).getClass());
	}

	@Test
	void shouldRegisterTypeHandler() {
		registry.register(NoneTypeHandler.class);
	}

	@Test
	void demoTypeHandlerForSuperInterface() {
		registry.register(SomeInterfaceTypeHandler.class);

		assertNull(registry.getTypeHandler(SomeClass.class), "Registering interface works only for enums.");
		assertSame(EnumTypeHandler.class, registry.getTypeHandler(NoTypeHandlerInterfaceEnum.class).getClass(), "When type handler for interface is not exist, apply default enum type handler.");
		assertSame(SomeInterfaceTypeHandler.class, registry.getTypeHandler(SomeEnum.class).getClass());
		assertSame(SomeInterfaceTypeHandler.class, registry.getTypeHandler(ExtendingSomeEnum.class).getClass());
		assertSame(SomeInterfaceTypeHandler.class, registry.getTypeHandler(ImplementingMultiInterfaceSomeEnum.class).getClass());
	}

	@Test
	void shouldRegisterReplaceNullMap() {
		class Address {}

		assertFalse(registry.hasTypeHandler(Address.class));
		registry.register(Address.class, StringTypeHandler.class);
		assertTrue(registry.hasTypeHandler(Address.class));
	}

	@Test
	void shouldRegisterEnumType() {
		assertTrue(registry.hasTypeHandler(TestEnum.class));
	}

	@Test
	void shouldAutoRegisterEnumTypeInMultiThreadEnvironment() throws ExecutionException, InterruptedException {

		ExecutorService executorService = Executors.newCachedThreadPool();

		try {
			for (int i = 0; i < 2000; i++) {
				TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
				List<Future<Boolean>> taskResults = IntStream.range(0, 2)
						.mapToObj(index -> executorService.submit(() -> typeHandlerRegistry.hasTypeHandler(TestEnum.class, JdbcType.VARCHAR)))
						.toList();

				for (Future<Boolean> future : taskResults) {
					assertTrue(future.get(), "false is returned at round " + i);
				}
			}
		}
		finally {
			executorService.shutdownNow();
		}
	}

	interface SomeInterface {}

	interface ExtendingSomeInterface extends SomeInterface {}

	interface NoTypeHandlerInterface {}

	enum SomeEnum implements SomeInterface {}

	enum ExtendingSomeEnum implements ExtendingSomeInterface {}

	enum ImplementingMultiInterfaceSomeEnum implements NoTypeHandlerInterface, ExtendingSomeInterface {}

	enum NoTypeHandlerInterfaceEnum implements NoTypeHandlerInterface {}

	class SomeClass implements SomeInterface {}

	@MappedTypes(SomeInterface.class)
	public static class SomeInterfaceTypeHandler<E extends Enum<E> & SomeInterface> extends BaseTypeHandler<E> {
		@Override
		public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {

		}

		@Override
		public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
			return null;
		}

		@Override
		public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
			return null;
		}

		@Override
		public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
			return null;
		}
	}

	public static class NoneTypeHandler extends BaseTypeHandler<String> {

		@Override
		public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {

		}

		@Override
		public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
			return "";
		}

		@Override
		public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
			return "";
		}

		@Override
		public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
			return "";
		}
	}

	enum TestEnum {
		ONE,
		TWO
	}

	enum TestEnum2 implements Serializable {
		ONE,
		TWO
	}

	@Test
	void shouldRegisterEnum2TypeHandler() {
		registry.getTypeHandler(TestEnum2.class);
	}
}