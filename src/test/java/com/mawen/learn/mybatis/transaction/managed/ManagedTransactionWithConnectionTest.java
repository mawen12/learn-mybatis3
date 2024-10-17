package com.mawen.learn.mybatis.transaction.managed;

import java.sql.Connection;
import java.sql.SQLException;

import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManagedTransactionWithConnectionTest extends TransactionTest {

	@Mock
	private Connection connection;

	private Transaction transaction;

	@BeforeEach
	void setup() {
		this.transaction = new ManagedTransaction(connection, true);
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
		transaction.commit();

		verify(connection, never()).commit();
	}

	@Test
	@Override
	public void shouldRollback() throws SQLException {
		transaction.commit();

		verify(connection, never()).rollback();
	}

	@Test
	@Override
	public void shouldClose() throws SQLException {
		transaction.close();

		verify(connection).close();
	}

	@Test
	void shouldNotCloseWhenSetNonCloseConnection() throws SQLException {
		this.transaction = new ManagedTransaction(connection, false);

		transaction.close();

		verify(connection, never()).close();
	}

	@Test
	@Override
	public void shouldGetTimeout() throws SQLException {
		assertNull(transaction.getTimeout());
	}
}