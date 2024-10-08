package com.mawen.learn.mybatis.reflection.property;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class PropertyTokenizerTest {

	@Test
	void shouldParsePropertySuccessfully() {
		String fullname = "id";
		PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

		assertEquals("id", tokenizer.getIndexedName());
		assertEquals("id", tokenizer.getName());

		assertNull(tokenizer.getChildren());
		assertNull(tokenizer.getIndex());
		assertFalse(tokenizer.hasNext());
		assertNull(tokenizer.getIndex());

		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(tokenizer::remove)
				.withMessage("Remove is not supported, as it has no meaning in the context of properties.");
	}

	@Test
	void shouldParsePropertyWhichContainsDelimSuccessfully() {
		String fullname = "person.id";
		PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

		assertEquals("person", tokenizer.getIndexedName());
		assertEquals("person", tokenizer.getName());
		assertTrue(tokenizer.hasNext());
		assertEquals("id", tokenizer.getChildren());

		assertNull(tokenizer.getIndex());

		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(tokenizer::remove)
				.withMessage("Remove is not supported, as it has no meaning in the context of properties.");
	}

	@Test
	void shouldParsePropertyWhichContainsIndexSuccessfully() {
		String fullname = "array[0]";
		PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

		assertEquals("array[0]", tokenizer.getIndexedName());
		assertEquals("array", tokenizer.getName());
		assertEquals("0", tokenizer.getIndex());

		assertFalse(tokenizer.hasNext());
		assertNull(tokenizer.getChildren());

		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(tokenizer::remove)
				.withMessage("Remove is not supported, as it has no meaning in the context of properties.");
	}
}