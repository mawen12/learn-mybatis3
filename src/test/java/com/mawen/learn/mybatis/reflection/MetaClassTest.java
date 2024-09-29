package com.mawen.learn.mybatis.reflection;

import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.domain.misc.RichType;
import com.mawen.learn.mybatis.domain.misc.generics.GenericConcrete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetaClassTest {

	private ReflectorFactory reflectorFactory;

	@BeforeEach
	void setup() {
		reflectorFactory = new DefaultReflectorFactory();
	}

	@Test
	void shouldTestDataTypeOfGenericMethod() {
		MetaClass meta = MetaClass.forClass(GenericConcrete.class, reflectorFactory);
		assertEquals(Long.class, meta.getGetterType("id"));
		assertEquals(Long.class, meta.getSetterType("id"));
	}

	@Test
	void shouldThrowReflectionExceptionGetGetterType() {
		MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
		assertThrows(ReflectionException.class, () -> {
			meta.getGetterType("aString");
		}, "There is no getter for property named \'aString\' in \'class org.apache.ibatis.domain.misc.RichType\'");
	}

	@Test
	void shouldCheckGetterExistance() {
		MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);

		assertTrue(meta.hasGetter("richField"));
		assertTrue(meta.hasGetter("richProperty"));
		assertTrue(meta.hasGetter("richList"));
		assertTrue(meta.hasGetter("richMap"));
		assertTrue(meta.hasGetter("richList[0]"));

		assertTrue(meta.hasGetter("richType"));
		assertTrue(meta.hasGetter("richType.richField"));
		assertTrue(meta.hasGetter("richType.richProperty"));
		assertTrue(meta.hasGetter("richType.richList"));
		assertTrue(meta.hasGetter("richType.richMap"));
		assertTrue(meta.hasGetter("richType.richList[0]"));

		assertEquals("richType.richProperty", meta.findProperty("richType.richProperty", false));

		assertFalse(meta.hasGetter("[0]"));
	}

	@Test
	void shouldCheckSetterExistance() {
		MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);

		assertEquals(String.class, meta.getGetterType("richField"));
		assertEquals(String.class, meta.getGetterType("richProperty"));
		assertEquals(List.class ,meta.getGetterType("richList"));
		assertEquals(Map.class, meta.getGetterType("richMap"));
		assertEquals(List.class, meta.getGetterType("richList[0]"));

		assertEquals(RichType.class, meta.getGetterType("richType"));
		assertEquals(String.class, meta.getGetterType("richType.richField"));
		assertEquals(String.class, meta.getGetterType("richType.richProperty"));
		assertEquals(List.class, meta.getGetterType("richType.richList"));
		assertEquals(Map.class, meta.getGetterType("richType.richMap"));
		assertEquals(List.class, meta.getGetterType("richType.richList[0]"));
	}

	@Test
	void shouldCheckTypeForEachSetter() {
		MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);

		assertEquals(String.class, meta.getSetterType("richField"));
		assertEquals(String.class, meta.getSetterType("richProperty"));
		assertEquals(List.class, meta.getSetterType("richList"));
		assertEquals(Map.class, meta.getSetterType("richMap"));
		assertEquals(List.class, meta.getSetterType("richList[0]"));

		assertEquals(RichType.class, meta.getSetterType("richType"));
		assertEquals(String.class, meta.getSetterType("richType.richField"));
		assertEquals(String.class, meta.getSetterType("richType.richProperty"));
		assertEquals(List.class, meta.getSetterType("richType.richList"));
		assertEquals(Map.class, meta.getSetterType("richType.richMap"));
		assertEquals(List.class, meta.getSetterType("richType.richList[0]"));
	}

	@Test
	void shouldCheckGetterAndSetterNames() {
		MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);

		assertEquals(5, meta.getGetterNames().length);
		assertEquals(5, meta.getSetterNames().length);
	}

	@Test
	void shouldFindPropertyName() {
		MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
		assertEquals("richField", meta.findProperty("RICHfield"));
	}
}