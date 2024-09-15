package com.mawen.learn.mybatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class InterceptorChain {

	private final List<Interceptor> interceptors = new ArrayList<>();

	public Object pluginAll(Object target) {
		for (Interceptor interceptor : interceptors) {
			target = interceptor.plugin(target);
		}
		return target;
	}

	public void addInterceptor(Interceptor interceptor) {
		interceptors.add(interceptor);
	}

	public List<Interceptor> getInterceptors() {
		return Collections.unmodifiableList(interceptors);
	}
}
