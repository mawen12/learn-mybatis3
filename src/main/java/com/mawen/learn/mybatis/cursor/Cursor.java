package com.mawen.learn.mybatis.cursor;

import java.io.Closeable;
import java.util.Iterator;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public interface Cursor<T> extends Closeable, Iterable<T> {

	boolean isOpen();

	boolean isConsumed();

	int getCurrentIndex();
}
