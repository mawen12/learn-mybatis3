package com.mawen.learn.mybatis.executor;

import java.sql.BatchUpdateException;
import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/14
 */
public class BatchExecutorException extends ExecutorException {

	private static final long serialVersionUID = -1331782497198526874L;

	private final List<BatchResult> successfulBatchResults;
	private final BatchUpdateException batchUpdateException;
	private final BatchResult batchResult;

	public BatchExecutorException(String message, BatchUpdateException cause, List<BatchResult> successfulBatchResults, BatchResult batchResult) {
		super(message + " Cause: " + cause, cause);
		this.batchUpdateException = cause;
		this.successfulBatchResults = successfulBatchResults;
		this.batchResult = batchResult;
	}

	public List<BatchResult> getSuccessfulBatchResults() {
		return successfulBatchResults;
	}

	public BatchUpdateException getBatchUpdateException() {
		return batchUpdateException;
	}

	public String getFailingSqlStatement() {
		return batchResult.getSql();
	}

	public String getFailingStatementId() {
		return batchResult.getMappedStatement().getId();
	}
}
