package com.mawen.learn.mybatis.cursor.defaults;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.mawen.learn.mybatis.cursor.Cursor;
import com.mawen.learn.mybatis.executor.resultset.DefaultResultSetHandler;
import com.mawen.learn.mybatis.executor.resultset.ResultSetWrapper;
import com.mawen.learn.mybatis.mapping.ResultMap;
import com.mawen.learn.mybatis.session.ResultContext;
import com.mawen.learn.mybatis.session.ResultHandler;
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
	private final ObjectWrapperResultHandler<T> objectWrapperResultHandler = new ObjectWrapperResultHandler<>();

	private final CursorIterator cursorIterator = new CursorIterator();
	private boolean iteratorRetrieved;

	private CursorStatus status = CursorStatus.CREATED;
	private int indexWithRowBound = -1;

	public DefaultCursor(DefaultResultSetHandler resultSetHandler, ResultMap resultMap, ResultSetWrapper rsw, RowBounds rowBounds) {
		this.resultSetHandler = resultSetHandler;
		this.resultMap = resultMap;
		this.rsw = rsw;
		this.rowBounds = rowBounds;
	}

	@Override
	public boolean isOpen() {
		return status == CursorStatus.OPEN;
	}

	@Override
	public boolean isConsumed() {
		return status == CursorStatus.CONSUMED;
	}

	@Override
	public int getCurrentIndex() {
		return rowBounds.getOffset() + cursorIterator.iteratorIndex;
	}

	@Override
	public void close() {
		if (isClosed()) {
			return;
		}

		ResultSet rs = rsw.getResultSet();
		try {
			if (rs != null) {
				rs.close();
			}
		}
		catch (SQLException ignored) {

		}
		finally {
			status = CursorStatus.CLOSED;
		}
	}

	@Override
	public Iterator<T> iterator() {
		if (iteratorRetrieved) {
			throw new IllegalStateException("Cannot open more than one iterator on a Cursor");
		}

		if (isClosed()) {
			throw new IllegalStateException("A Cursor is already closed.");
		}
		this.iteratorRetrieved = true;
		return cursorIterator;
	}

	protected T fetchNextUsingRowBound() {
		T result = fetchNextObjectFromDatabase();
		while (objectWrapperResultHandler.fetched && indexWithRowBound < rowBounds.getOffset()) {
			result = fetchNextObjectFromDatabase();
		}
		return result;
	}

	protected T fetchNextObjectFromDatabase() {
		if (isClosed()) {
			return null;
		}

		try {
			objectWrapperResultHandler.fetched = false;
			status = CursorStatus.OPEN;
			if (!rsw.getResultSet().isClosed()) {
				resultSetHandler.handleRowValues(rsw, resultMap,objectWrapperResultHandler,RowBounds.DEFAULT, null);
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}

		T next = objectWrapperResultHandler.result;
		if (objectWrapperResultHandler.fetched) {
			indexWithRowBound++;
		}

		if (!objectWrapperResultHandler.fetched || getReadItemsCount() == rowBounds.getOffset() + rowBounds.getLimit()) {
			close();
			status = CursorStatus.CONSUMED;
		}

		objectWrapperResultHandler.result = null;
		return next;
	}

	private int getReadItemsCount() {
		return indexWithRowBound + 1;
	}

	private boolean isClosed() {
		return status == CursorStatus.CLOSED || status == CursorStatus.CONSUMED;
	}

	private enum CursorStatus {

		CREATED,

		OPEN,

		CLOSED,

		CONSUMED;
	}

	protected static class ObjectWrapperResultHandler<T> implements ResultHandler<T> {

		protected T result;
		protected boolean fetched;

		@Override
		public void handleResult(ResultContext<? extends T> resultContext) {
			this.result = resultContext.getResultObject();
			resultContext.stop();
			this.fetched = true;
		}
	}

	protected class CursorIterator implements Iterator<T> {

		T object;

		int iteratorIndex = -1;

		@Override
		public boolean hasNext() {
			if (!objectWrapperResultHandler.fetched) {
				object = fetchNextUsingRowBound();
			}
			return objectWrapperResultHandler.fetched;
		}

		@Override
		public T next() {

			T next = object;

			if (!objectWrapperResultHandler.fetched) {
				next = fetchNextUsingRowBound();
			}

			if (objectWrapperResultHandler.fetched) {
				objectWrapperResultHandler.fetched = false;
				object = null;
				iteratorIndex++;
				return next;
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove element from Cursor");
		}
	}
}
