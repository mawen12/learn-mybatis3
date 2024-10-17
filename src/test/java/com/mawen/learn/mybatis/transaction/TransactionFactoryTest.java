package com.mawen.learn.mybatis.transaction;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class TransactionFactoryTest {

	public abstract void shouldSetProperties() throws Exception;

	public abstract void shouldNewTransactionWithConnection() throws SQLException;

	public abstract void shouldNewTransactionWithDataSource() throws Exception;

	public static Object getValue(Field field, Object object) throws Exception {
		field.setAccessible(true);
		return field.get(object);
	}
}