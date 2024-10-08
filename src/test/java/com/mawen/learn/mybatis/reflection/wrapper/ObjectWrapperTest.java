package com.mawen.learn.mybatis.reflection.wrapper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
abstract class ObjectWrapperTest {

	abstract void shouldGet();

	abstract void shouldSet();

	abstract void shouldFindProperty();

	abstract void shouldGetGetterNames();

	abstract void shouldGetSetterNames();

	abstract void shouldGetGetterType();

	abstract void shouldGetSetterType();

	abstract void shouldHasGetter();

	abstract void shouldHasSetter();

	abstract void shouldIsCollection();

	abstract void shouldInstantiatePropertyValue();

	abstract void shouldAddElement();

	abstract void shouldAddAll();
}