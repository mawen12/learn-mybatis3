package com.mawen.learn.mybatis.cursor.defaults;

import java.io.IOException;
import java.util.Iterator;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.session.RowBounds;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class DefaultCursor<T> implements Cursor<T> {

	private final DefaultResultSetHandler resultSetHandler;
	private final ResultMap resultMap;
	private final ResultSetWrapper rsw;
	private final RowBounds rowBounds;
	private final ObjectWrapperResultHandler<T> objectWrapperResultHandler = new ObjectWrapperResultHandler();


	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isConsumed() {
		return false;
	}

	@Override
	public int getCurrentIndex() {
		return 0;
	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public Iterator<T> iterator() {
		return null;
	}
}
