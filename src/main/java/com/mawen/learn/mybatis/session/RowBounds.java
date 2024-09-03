package com.mawen.learn.mybatis.session;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/3
 */
public class RowBounds {

	public static final int NO_ROW_OFFSET = 0;
	public static final int NO_ROW_LIMIT = Integer.MAX_VALUE;
	public static final RowBounds DEFAULT = new RowBounds();

	private final int offset;
	private final int limit;

	public RowBounds() {
		this.offset = NO_ROW_OFFSET;
		this.limit = NO_ROW_LIMIT;
	}

	public RowBounds(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}
}
