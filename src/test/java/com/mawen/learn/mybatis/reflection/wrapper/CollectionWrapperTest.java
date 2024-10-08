package com.mawen.learn.mybatis.reflection.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mawen.learn.mybatis.reflection.MetaObject;
import com.mawen.learn.mybatis.reflection.SystemMetaObject;
import com.mawen.learn.mybatis.reflection.property.PropertyTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionWrapperTest extends ObjectWrapperTest{

	@Mock
	private Collection<Object> collection;

	@Mock
	private PropertyTokenizer tokenizer;

	private ObjectWrapper wrapper;

	@BeforeEach
	void setup() {
		MetaObject metaObject = SystemMetaObject.forObject(collection);
		this.wrapper = new CollectionWrapper(metaObject, collection);
	}

	@Test
	@Override
	void shouldGet() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.get(tokenizer));
	}

	@Test
	@Override
	void shouldSet() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.set(tokenizer, null));
	}

	@Test
	@Override
	void shouldFindProperty() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.findProperty("abc", true));
	}

	@Test
	@Override
	void shouldGetGetterNames() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.getGetterNames());
	}

	@Test
	@Override
	void shouldGetSetterNames() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.getSetterNames());
	}

	@Test
	@Override
	void shouldGetGetterType() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.getGetterType("abc"));
	}

	@Test
	@Override
	void shouldGetSetterType() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.getSetterType("abc"));
	}

	@Test
	@Override
	void shouldHasGetter() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.hasGetter("abc"));
	}

	@Test
	@Override
	void shouldHasSetter() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.hasSetter("abc"));
	}

	@Test
	@Override
	void shouldIsCollection() {
		assertTrue(wrapper.isCollection());
	}

	@Test
	@Override
	void shouldInstantiatePropertyValue() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> wrapper.instantiatePropertyValue("abc", tokenizer, null));
	}

	@Test
	@Override
	void shouldAddElement() {
		wrapper.add("bdc");

		verify(collection).add("bdc");
	}

	@Test
	@Override
	void shouldAddAll() {
		List<Object> list = new ArrayList<>() {{
			add("1");
			add("2");
			add("3");
		}};
		wrapper.addAll(list);

		verify(collection).addAll(list);
	}
}