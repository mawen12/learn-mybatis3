package com.mawen.learn.mybatis.transaction.managed;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.mawen.learn.mybatis.session.TransactionIsolationLevel;
import com.mawen.learn.mybatis.transaction.Transaction;
import com.mawen.learn.mybatis.transaction.TransactionFactory;
import com.mawen.learn.mybatis.transaction.TransactionFactoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManagedTransactionFactoryTest extends TransactionFactoryTest {

	@Mock
	private Properties properties;

	@Mock
	private Connection connection;

	@Mock
	private DataSource dataSource;

	private TransactionFactory transactionFactory;

	@BeforeEach
	void setup() {
		this.transactionFactory = new ManagedTransactionFactory();
	}

	@Test
	@Override
	public void shouldSetProperties() throws Exception {
		when(properties.getProperty("closeConnection")).thenReturn("false");

		transactionFactory.setProperties(properties);

		assertFalse((Boolean) getValue(transactionFactory.getClass().getDeclaredField("closeConnection"), transactionFactory));
	}

	@Test
	@Override
	public void shouldNewTransactionWithConnection() throws SQLException {
		Transaction result = transactionFactory.newTransaction(connection);

		assertNotNull(result);
		assertInstanceOf(ManagedTransaction.class, result);
		assertEquals(connection, result.getConnection());
	}

	@Test
	@Override
	public void shouldNewTransactionWithDataSource() throws Exception {
		when(dataSource.getConnection()).thenReturn(connection);
		when(properties.getProperty("closeConnection")).thenReturn("false");

		transactionFactory.setProperties(properties);
		Transaction result = transactionFactory.newTransaction(dataSource, TransactionIsolationLevel.READ_COMMITTED, true);

		assertNotNull(result);
		assertInstanceOf(ManagedTransaction.class, result);
		assertEquals(connection, result.getConnection());
		verify(connection).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

		assertEquals(dataSource, getValue(result.getClass().getDeclaredField("dataSource"), result));
		assertEquals(TransactionIsolationLevel.READ_COMMITTED, getValue(result.getClass().getDeclaredField("level"), result));
		assertEquals(false, getValue(result.getClass().getDeclaredField("closeConnection"), result));
	}
}