package com.mawen.learn.mybatis.reflection.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertyTokenizerTest {

	@Test
	void shouldParseSinglePropertySuccessfully() {
		String fullname = "id";
		PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

		assertEquals(fullname, tokenizer.getName());
		assertEquals(fullname, tokenizer.getIndexedName());
		assertNull(tokenizer.getChildren());
		assertNull(tokenizer.getIndex());
		assertFalse(tokenizer.hasNext());
		assertNull(tokenizer.next());
	}

	@Test
	void shouldParseComplexPropertySuccessfully() {
		String fullname = "person.id";
		PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

		assertEquals("person", tokenizer.getName());
		assertEquals("person", tokenizer.getIndexedName());
		assertEquals("id", tokenizer.getChildren());

	}

}