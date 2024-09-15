package com.mawen.learn.mybatis.executor.statement;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/12
 */
public class StatementUtil {

	public static void applyTransactionTimeout(Statement statement, Integer queryTimeout, Integer transactionTimeout) throws SQLException {
		if (transactionTimeout == null) {
			return;
		}

		if (queryTimeout == null || queryTimeout == 0 || transactionTimeout < queryTimeout) {
			statement.setQueryTimeout(transactionTimeout);
		}
	}

	private StatementUtil() {}
}
