package com.mawen.learn.mybatis.transaction.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JdbcTransactionWithConnectionTest extends TransactionTest {

	@Mock
	private Connection connection;

	private Transaction transaction;

	@BeforeEach
	void setup() {
		this.transaction = new JdbcTransaction(connection);
	}

	@Test
	@Override
	public void shouldGetConnection() throws SQLException {
		Connection result = transaction.getConnection();

		assertEquals(connection, result);
	}

	@Test
	@Override
	public void shouldCommit() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(false);

		transaction.commit();

		verify(connection).commit();
	}

	@Test
	void shouldAutoCommit() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(true);

		transaction.commit();

		verify(connection, never()).commit();
	}

	@Test
	@Override
	public void shouldRollback() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(false);

		transaction.rollback();

		verify(connection).rollback();
	}

	@Test
	void shouldAutoRollback() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(true);

		transaction.rollback();

		verify(connection, never()).rollback();
	}

	@Test
	@Override
	public void shouldClose() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(false);

		transaction.close();

		verify(connection).close();
		verify(connection).setAutoCommit(true);
	}

	@Test
	void shouldCloseWithAutoCommit() throws SQLException {
		when(connection.getAutoCommit()).thenReturn(true);

		transaction.close();

		verify(connection).close();
		verify(connection, never()).setAutoCommit(true);
	}

	@Test
	@Override
	public void shouldGetTimeout() throws SQLException {
		assertNull(transaction.getTimeout());
	}
}