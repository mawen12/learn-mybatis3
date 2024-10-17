package com.mawen.learn.mybatis.transaction.managed;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManagedTransactionWithDataSourceTest extends TransactionTest {

	@Mock
	private DataSource dataSource;

	@Mock
	private Connection connection;

	private Transaction transaction;

	@BeforeEach
	void setup() {
		this.transaction = new ManagedTransaction(dataSource, TransactionIsolationLevel.READ_COMMITTED, true);
	}

	@Test
	@Override
	public void shouldGetConnection() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);

		Connection result = transaction.getConnection();

		assertEquals(connection, result);
		verify(connection).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	}

	@Test
	@Override
	public void shouldCommit() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);

		transaction.getConnection();
		transaction.commit();

		verify(connection, never()).commit();
	}

	@Test
	@Override
	public void shouldRollback() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);

		transaction.getConnection();
		transaction.rollback();

		verify(connection, never()).rollback();
	}

	@Test
	@Override
	public void shouldClose() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);

		transaction.getConnection();
		transaction.close();

		verify(connection).close();
	}

	@Test
	void shouldNotCloseWhenSetNonCloseConnection() throws SQLException {
		this.transaction = new ManagedTransaction(dataSource, TransactionIsolationLevel.READ_COMMITTED, false);
		when(dataSource.getConnection()).thenReturn(connection);

		transaction.getConnection();
		transaction.close();

		verify(connection, never()).close();
	}

	@Test
	@Override
	public void shouldGetTimeout() throws SQLException {
		assertNull(transaction.getTimeout());
	}
}