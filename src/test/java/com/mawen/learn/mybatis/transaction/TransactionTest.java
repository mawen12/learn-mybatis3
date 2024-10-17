package com.mawen.learn.mybatis.transaction;

import java.sql.SQLException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public abstract class TransactionTest {

	public abstract void shouldGetConnection() throws SQLException;

	public abstract void shouldCommit() throws SQLException;

	public abstract void shouldRollback() throws SQLException;

	public abstract void shouldClose() throws SQLException;

	public abstract void shouldGetTimeout() throws SQLException;
}