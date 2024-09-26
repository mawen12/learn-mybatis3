package com.mawen.learn.mybatis.type;

import java.util.HashMap;

import com.mawen.learn.mybatis.domain.misc.RichType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTypeRegistryTest {

	@Test
	void shouldTestIfClassIsSimpleTypeAndReturnType() {
		assertTrue(SimpleTypeRegistry.isSimpleType(String.class));
	}

	@Test
	void shouldTestIfClassIsSimpleTypeAndReturnFalse() {
		assertFalse(SimpleTypeRegistry.isSimpleType(RichType.class));
	}

	@Test
	void shouldTestIfMapIsSimpleTypeAndReturnFalse() {
		assertFalse(SimpleTypeRegistry.isSimpleType(HashMap.class));
	}
}