package com.mawen.learn.mybatis.plugin;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginTest {

	@Test
	void mapPluginShouldInterceptGet() {
		Map map = new HashMap<>();
		map = (Map) new AlwaysMapPlugin().plugin(map);
		assertEquals("Always", map.get("anything"));
	}

	@Test
	void shouldNotInterceptToString() {
		Map map = new HashMap();
		map = (Map) new AlwaysMapPlugin().plugin(map);
		assertNotEquals("Always", map.toString());
	}

	@Intercepts(
			@Signature(type = Map.class, method = "get", args = {Object.class})
	)
	public static class AlwaysMapPlugin implements Interceptor {

		@Override
		public Object interceptor(Invocation invocation) throws Throwable {
			return "Always";
		}

	}
}