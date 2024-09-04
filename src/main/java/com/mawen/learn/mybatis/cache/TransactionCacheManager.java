package com.mawen.learn.mybatis.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class TransactionCacheManager {

	private final Map<Cache, TransactionCache> transactionCaches = new HashMap<>();
}
