package com.mawen.learn.mybatis.reflection.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mawen.learn.mybatis.domain.misc.RichType;
import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.SystemMetaObject;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class BeanWrapperTest extends ObjectWrapperTest {

	private RichType richType;

	private ObjectWrapper wrapper;

	@BeforeEach
	void setup() {
		this.richType = new RichType();
		MetaObject metaObject = SystemMetaObject.forObject(richType);
		this.wrapper = new BeanWrapper(metaObject, richType);
	}

	@Test
	@Override
	void shouldGet() {
		richType.setRichProperty("mybatis");

		Object value = wrapper.get(new PropertyTokenizer("richProperty"));

		assertEquals("mybatis", value);
	}

	@Test
	void shouldGetWhichContainsDelim() {
		RichType nested = new RichType();
		richType.setRichType(nested);

		Object value = wrapper.get(new PropertyTokenizer("richType.richProperty"));

		assertEquals(nested, value);
	}

	@Test
	void shouldGetWhichContainsIndex() {
		richType.setRichList(Arrays.asList(1L, "abc"));

		Object value = wrapper.get(new PropertyTokenizer("richList[1]"));

		assertEquals("abc", value);
	}

	@Test
	@Override
	void shouldSet() {
		wrapper.set(new PropertyTokenizer("richProperty"), "mybatis");

		assertEquals("mybatis", richType.getRichProperty());
	}

	@Test
	void shouldSetWhichContainsIndex() {
		List<Object> list = Arrays.asList(1L, 2L);
		richType.setRichList(list);

		wrapper.set(new PropertyTokenizer("richList[0]"), "mybatis");

		assertEquals("mybatis", list.get(0));
	}

	@Test
	@Override
	void shouldFindProperty() {
		String property = wrapper.findProperty("richType.richProperty", false);

		assertEquals("richType.richProperty", property);
	}

	@Test
	@Override
	void shouldGetGetterNames() {
		String[] getterNames = wrapper.getGetterNames();

		assertThat(getterNames).containsExactlyInAnyOrder("richType", "richProperty", "richList", "richMap", "richField");
	}

	@Test
	@Override
	void shouldGetSetterNames() {
		String[] setterNames = wrapper.getSetterNames();

		assertThat(setterNames).containsExactlyInAnyOrder("richType", "richProperty", "richList", "richMap", "richField");
	}

	@Test
	@Override
	void shouldGetGetterType() {
		assertEquals(RichType.class, wrapper.getGetterType("richType"));
		assertEquals(String.class, wrapper.getGetterType("richField"));
		assertEquals(String.class, wrapper.getGetterType("richProperty"));
		assertEquals(Map.class, wrapper.getGetterType("richMap"));
		assertEquals(List.class, wrapper.getGetterType("richList"));
	}

	@Test
	@Override
	void shouldGetSetterType() {
		assertEquals(RichType.class, wrapper.getSetterType("richType"));
		assertEquals(String.class, wrapper.getSetterType("richField"));
		assertEquals(String.class, wrapper.getSetterType("richProperty"));
		assertEquals(Map.class, wrapper.getSetterType("richMap"));
		assertEquals(List.class, wrapper.getSetterType("richList"));
	}

	@Test
	@Override
	void shouldHasGetter() {
		assertTrue(wrapper.hasGetter("richType"));
		assertTrue(wrapper.hasGetter("richField"));
		assertTrue(wrapper.hasGetter("richProperty"));
		assertTrue(wrapper.hasGetter("richMap"));
		assertTrue(wrapper.hasGetter("richList"));
	}

	@Test
	@Override
	void shouldHasSetter() {
		assertTrue(wrapper.hasGetter("richType"));
		assertTrue(wrapper.hasGetter("richField"));
		assertTrue(wrapper.hasGetter("richProperty"));
		assertTrue(wrapper.hasGetter("richMap"));
		assertTrue(wrapper.hasGetter("richList"));
	}

	@Test
	@Override
	void shouldIsCollection() {
		assertFalse(wrapper.isCollection());
	}

	@Test
	@Override
	void shouldInstantiatePropertyValue() {
		// Nothing
	}

	@Test
	@Override
	void shouldAddElement() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.add("1"));
	}

	@Test
	@Override
	void shouldAddAll() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.addAll(new ArrayList<>()));
	}
}