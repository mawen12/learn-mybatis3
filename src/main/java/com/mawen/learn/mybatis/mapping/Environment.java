package com.mawen.learn.mybatis.mapping;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.transaction.TransactionFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/30
 */
public class Environment {

	private final String id;
	private final TransactionFactory transactionFactory;
	private final DataSource dataSource;

	public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
		if (id == null) {
			throw new IllegalArgumentException("Parameter 'id' must not be null");
		}
		if (transactionFactory == null) {
			throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
		}
		if (dataSource == null) {
			throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
		}

		this.id = id;
		this.transactionFactory = transactionFactory;
		this.dataSource = dataSource;
	}

	public static class Builder {

		private final String id;
		private TransactionFactory transactionFactory;
		private DataSource dataSource;

		public Builder(String id) {
			this.id = id;
		}

		public Builder transactionFactory(TransactionFactory transactionFactory) {
			this.transactionFactory = transactionFactory;
			return this;
		}

		public Builder dataSource(DataSource dataSource) {
			this.dataSource = dataSource;
			return this;
		}

		public String id() {
			return id;
		}

		public Environment build() {
			return new Environment(id, transactionFactory, dataSource);
		}
	}

	public String getId() {
		return id;
	}

	public TransactionFactory getTransactionFactory() {
		return transactionFactory;
	}

	public DataSource getDataSource() {
		return dataSource;
	}
}
