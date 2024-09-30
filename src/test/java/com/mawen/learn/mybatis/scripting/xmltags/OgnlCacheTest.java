package com.mawen.learn.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OgnlCacheTest {

	@Test
	void concurrentAccess() throws ExecutionException, InterruptedException {
		class DataClass {
			private int id;
		}

		int run = 1000;
		Map<String, Object> context = new HashMap<>();
		List<Future<Object>> futures = new ArrayList<>();
		context.put("data", new DataClass());

		ExecutorService executor = Executors.newCachedThreadPool();
		IntStream.range(0, run).forEach(i -> {
			futures.add(executor.submit(() -> {
				return OgnlCache.getValue("data.id", context);
			}));
		});

		for (int i = 0; i < run; i++) {
			assertEquals(0, futures.get(i).get());
		}

		executor.shutdown();
	}
}