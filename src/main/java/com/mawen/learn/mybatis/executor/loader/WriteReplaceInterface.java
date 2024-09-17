package com.mawen.learn.mybatis.executor.loader;

import java.io.ObjectStreamException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/17
 */
public interface WriteReplaceInterface {

	Object writeReplace() throws ObjectStreamException;
}
