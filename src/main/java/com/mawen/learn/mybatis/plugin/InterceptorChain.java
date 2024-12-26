package com.mawen.learn.mybatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 存放从全局配置文件读取到的插件列表。
 * 并提供将插件列表统一应用拦截的功能。
 * 插件使用了责任链的设计模式。
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/15
 */
public class InterceptorChain {

	/**
	 * TODO 如果配置了多个代理，那么建议此处使用 LinkedList，因为不会关心元素索引。
	 */
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
