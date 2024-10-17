package com.mawen.learn.mybatis.transaction.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BooleanSupplier;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.internal.verification.Only;
import org.mockito.internal.verification.Times;
import org.mockito.verification.VerificationMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/10/17
 */
public class JdbcTransactionWithDataSourceTest extends TransactionTest {

	@Mock
	private DataSource dataSource;

	@Mock
	private Connection connection;

	@Mock
	private BooleanSupplier desiredAutoCommit;

	@Mock
	private BooleanSupplier skipSetAutoCommitClose;

	private Transaction transaction;

	@Test
	@Override
	public void shouldGetConnection() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(desiredAutoCommit.getAsBoolean()).thenReturn(true);
		when(connection.getAutoCommit()).thenReturn(false);

		buildTransaction();
		Connection result = transaction.getConnection();

		assertEquals(connection, result);
		verify(dataSource).getConnection();
		verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		verify(connection).setAutoCommit(true);
	}

	@Test
	void shouldGetConnectionWithNotAutoCommit() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(desiredAutoCommit.getAsBoolean()).thenReturn(false);
		when(connection.getAutoCommit()).thenReturn(true);

		buildTransaction();
		Connection result = transaction.getConnection();

		assertEquals(connection, result);
		verify(dataSource).getConnection();
		verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		verify(connection).setAutoCommit(false);
	}

	@Test
	@Override
	public void shouldCommit() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.getAutoCommit()).thenReturn(false);

		buildTransaction();
		transaction.getConnection();
		transaction.commit();

		verify(connection).commit();
	}

	@Test
	void shouldAutoCommit() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.getAutoCommit()).thenReturn(true);

		buildTransaction();
		transaction.getConnection();
		transaction.commit();

		verify(connection, never()).commit();
	}

	@Test
	@Override
	public void shouldRollback() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.getAutoCommit()).thenReturn(false);

		buildTransaction();
		transaction.getConnection();
		transaction.rollback();

		verify(connection).rollback();
	}

	@Test
	void shouldAutoRollback() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.getAutoCommit()).thenReturn(true);

		buildTransaction();
		transaction.getConnection();
		transaction.commit();

		verify(connection, never()).rollback();
	}

	@Test
	@Override
	public void shouldClose() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(desiredAutoCommit.getAsBoolean()).thenReturn(false);
		when(skipSetAutoCommitClose.getAsBoolean()).thenReturn(false);

		buildTransaction();
		transaction.getConnection();
		transaction.close();

		verify(connection).close();
		verify(connection).setAutoCommit(true);
	}

	@Test
	void shouldNotSetAutoCommitWhenConnectionIsAutoCommit() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(desiredAutoCommit.getAsBoolean()).thenReturn(false);
		when(skipSetAutoCommitClose.getAsBoolean()).thenReturn(false);
		when(connection.getAutoCommit()).thenReturn(false);

		buildTransaction();
		transaction.getConnection();
		transaction.close();

		verify(connection).close();
		verify(connection).setAutoCommit(true);
	}

	@Test
	void shouldNotSetAutoCommitWhenSkipSetAutoCommit() throws SQLException {
		when(dataSource.getConnection()).thenReturn(connection);
		when(desiredAutoCommit.getAsBoolean()).thenReturn(false);
		when(skipSetAutoCommitClose.getAsBoolean()).thenReturn(false);
		when(connection.getAutoCommit()).thenReturn(true);

		buildTransaction();
		transaction.getConnection();
		transaction.close();

		verify(connection).close();
		verify(connection, never()).setAutoCommit(true);
	}

	@Test
	@Override
	public void shouldGetTimeout() throws SQLException {
		buildTransaction();

		assertNull(transaction.getTimeout());
	}

	private void buildTransaction() {
		this.transaction = new JdbcTransaction(dataSource, TransactionIsolationLevel.REPEATABLE_READ, desiredAutoCommit.getAsBoolean(), skipSetAutoCommitClose.getAsBoolean());
	}
}
