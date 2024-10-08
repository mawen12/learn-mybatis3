package com.mawen.learn.mybatis.reflection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.domain.blog.Author;
import com.mawen.learn.mybatis.domain.blog.Section;
import com.mawen.learn.mybatis.domain.misc.CustomBeanWrapper;
import com.mawen.learn.mybatis.domain.misc.CustomBeanWrapperFactory;
import com.mawen.learn.mybatis.domain.misc.RichType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetaObjectTest {

	private RichType rich;
	private MetaObject metaRich;

	private Author author;
	private MetaObject metaAuthor;

	@BeforeEach
	void setup() {
		this.rich = new RichType();
		this.metaRich = SystemMetaObject.forObject(rich);

		this.author = new Author();
		this.metaAuthor = SystemMetaObject.forObject(author);
	}

	@Test
	void shouldGetAndSetField() {
		metaRich.setValue("richField", "foo");

		assertEquals("foo", metaRich.getValue("richField"));
	}

	@Test
	void shouldGetAndSetNestedField() {
		metaRich.setValue("richType.richField", "foo");

		assertEquals("foo", metaRich.getValue("richType.richField"));
	}

	@Test
	void shouldGetAndSetProperty() {
		metaRich.setValue("richProperty", "foo");

		assertEquals("foo", rich.getRichProperty());
		assertEquals("foo", metaRich.getValue("richProperty"));
	}

	@Test
	void shouldGetAndSetNestedProperty() {
		metaRich.setValue("richType.richProperty", "foo");

		assertEquals("foo", rich.getRichType().getRichProperty());
		assertEquals("foo", metaRich.getValue("richType.richProperty"));
	}

	@Test
	void shouldGetAndSetMapPair() {
		metaRich.setValue("richMap.key", "foo");

		assertEquals("foo", rich.getRichMap().get("key"));
		assertEquals("foo", metaRich.getValue("richMap.key"));
	}

	@Test
	void shouldGetAndSetMapPairUsingArraySyntax() {
		metaRich.setValue("richMap[key]", "foo");

		assertEquals("foo", rich.getRichMap().get("key"));
		assertEquals("foo", metaRich.getValue("richMap[key]"));
	}

	@Test
	void shouldGetAndSetNestedMapPair() {
		metaRich.setValue("richType.richMap.key", "foo");

		assertEquals("foo", rich.getRichType().getRichMap().get("key"));
		assertEquals("foo", metaRich.getValue("richType.richMap.key"));
	}

	@Test
	void shouldGetAndSetNestedMapPairUsingArraySyntax() {
		metaRich.setValue("richType.richMap[key]", "foo");

		assertEquals("foo", rich.getRichType().getRichMap().get("key"));
		assertEquals("foo", metaRich.getValue("richType.richMap[key]"));
	}

	@Test
	void shouldGetAndSetListItem() {
		metaRich.setValue("richList[0]", "foo");

		assertEquals("foo", rich.getRichList().get(0));
		assertEquals("foo", metaRich.getValue("richList[0]"));
	}

	@Test
	void shouldGetAndSetNestedListItem() {
		metaRich.setValue("richType.richList[0]", "foo");

		assertEquals("foo", rich.getRichType().getRichList().get(0));
		assertEquals("foo", metaRich.getValue("richType.richList[0]"));
	}

	@Test
	void shouldGetReadablePropertyNames() {
		String[] readables = metaRich.getGetterNames();
		assertEquals(5, readables.length);

		for (String readable : readables) {
			assertTrue(metaRich.hasGetter(readable));
			assertTrue(metaRich.hasSetter("richType." + readable));
		}
		assertTrue(metaRich.hasGetter("richType"));
	}

	@Test
	void shouldSetWriteablePropertyNames() {
		String[] writeables = metaRich.getSetterNames();
		assertEquals(5, writeables.length);

		for (String writeable : writeables) {
			assertTrue(metaRich.hasSetter(writeable));
			assertTrue(metaRich.hasSetter("richType." + writeable));
		}
		assertTrue(metaRich.hasSetter("richType"));
	}

	@Test
	void shouldSetPropertyOfNullNestedPropertyWithNull() {
		metaRich.setValue("richType.richProperty", null);

		assertNull(rich.getRichType());
		assertNull(metaRich.getValue("richType.richProperty"));
	}

	@Test
	void shouldGetPropertyOfNullNestedProperty() {
		assertNull(rich.getRichType());
		assertNull(metaRich.getValue("richType.richProperty"));
	}

	@Test
	void shouldVerifyHasReadablePropertiesReturnedByGetReadablePropertyNames() {
		for (String readable : metaAuthor.getGetterNames()) {
			assertTrue(metaAuthor.hasGetter(readable));
		}
	}

	@Test
	void shouldVerifyHasWriteablePropertiesReturnedByGetWriteablePropertyNames() {
		for (String writeable : metaAuthor.getSetterNames()) {
			assertTrue(metaAuthor.hasSetter(writeable));
		}
	}

	@Test
	void shouldSetAndGetProperties() {
		metaAuthor.setValue("email", "test");

		assertEquals("test", author.getEmail());
		assertEquals("test", metaAuthor.getValue("email"));
	}

	@Test
	void shouldVerifyPropertyTypes() {
		assertEquals(6, metaAuthor.getSetterNames().length);
		assertEquals(int.class, metaAuthor.getGetterType("id"));
		assertEquals(String.class, metaAuthor.getGetterType("username"));
		assertEquals(String.class, metaAuthor.getGetterType("password"));
		assertEquals(String.class, metaAuthor.getGetterType("email"));
		assertEquals(String.class, metaAuthor.getGetterType("bio"));
		assertEquals(Section.class, metaAuthor.getGetterType("favouriteSection"));
	}

	@Test
	void shouldDemonstrateDeeplyNestedMapProperties() {
		Map<String, String> map = new HashMap<>();
		MetaObject metaMap = SystemMetaObject.forObject(map);

		assertTrue(metaMap.hasSetter("id"));
		assertTrue(metaMap.hasSetter("name.first"));
		assertTrue(metaMap.hasSetter("address.street"));

		assertFalse(metaMap.hasGetter("id"));
		assertFalse(metaMap.hasGetter("name.first"));
		assertFalse(metaMap.hasGetter("address.street"));

		metaMap.setValue("id", "100");
		metaMap.setValue("name.first", "Clinton");
		metaMap.setValue("name.last", "Begin");
		metaMap.setValue("address.street", "1 Some Street");
		metaMap.setValue("address.city", "This city");
		metaMap.setValue("address.province", "A Province");
		metaMap.setValue("address.postal_code", "1A3 4B6");

		assertTrue(metaMap.hasGetter("id"));
		assertTrue(metaMap.hasGetter("name.first"));
		assertTrue(metaMap.hasGetter("address.street"));

		assertEquals(3, metaMap.getGetterNames().length);
		assertEquals(3, metaMap.getSetterNames().length);

		Map<String, String> name = (Map<String, String>) metaMap.getValue("name");

		Map<String, String> address = (Map<String, String>) metaMap.getValue("address");

		assertEquals("Clinton", name.get("first"));
		assertEquals("1 Some Street", address.get("street"));
	}

	@Test
	void shouldDemonstrateNullValueInMap() {
		Map<String, String> map = new HashMap<>();
		MetaObject metaMap = SystemMetaObject.forObject(map);

		assertFalse(metaMap.hasGetter("phone.home"));

		metaMap.setValue("phone", null);
		assertTrue(metaMap.hasGetter("phone"));
		assertTrue(metaMap.hasGetter("phone.home"));
		assertTrue(metaMap.hasGetter("phone.home.ext"));

		assertNull(metaMap.getValue("phone"));
		assertNull(metaMap.getValue("phone.home"));
		assertNull(metaMap.getValue("phone.home.ext"));

		metaMap.setValue("phone.office", "789");
		assertFalse(metaMap.hasGetter("phone.home"));
		assertFalse(metaMap.hasGetter("phone.home.ext"));
		assertEquals("789", metaMap.getValue("phone.office"));
		assertNotNull(metaMap.getValue("phone"));
		assertNull(metaMap.getValue("phone.home"));
	}

	@Test
	void shouldNotUseObjectWrapperFactoryByDefault() {
		assertTrue(!metaAuthor.getObjectWrapper().getClass().equals(CustomBeanWrapper.class));
	}

	@Test
	void shouldUseObjectWrapperFactoryWhenSet() {
		MetaObject metaObject = MetaObject.forObject(new Author(), SystemMetaObject.DEFAULT_OBJECT_FACTORY, new CustomBeanWrapperFactory(), new DefaultReflectorFactory());
		assertEquals(CustomBeanWrapper.class, metaObject.getObjectWrapper().getClass());

		metaObject = SystemMetaObject.forObject(new Author());
		assertNotEquals(CustomBeanWrapper.class, metaObject.getObjectWrapper().getClass());
	}

	@Test
	void shouldMethodHasGetterReturnTrueWhenListElementSet() {
		List<Object> param1 = new ArrayList<>();
		param1.add("firstParam");
		param1.add(222);
		param1.add(new Date());

		Map<String, Object> parametersEmulation = new HashMap<>();
		parametersEmulation.put("param1", param1);
		parametersEmulation.put("filterParams", param1);

		MetaObject meta = SystemMetaObject.forObject(parametersEmulation);

		assertEquals(param1.get(0), meta.getValue("filterParams[0]"));
		assertEquals(param1.get(1), meta.getValue("filterParams[1]"));
		assertEquals(param1.get(2), meta.getValue("filterParams[2]"));

		assertTrue(meta.hasGetter("filterParams[0]"));
		assertTrue(meta.hasGetter("filterParams[1]"));
		assertTrue(meta.hasGetter("filterParams[2]"));
	}
}