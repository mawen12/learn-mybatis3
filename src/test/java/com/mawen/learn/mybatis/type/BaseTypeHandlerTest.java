package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
@ExtendWith(MockitoExtension.class)
abstract class BaseTypeHandlerTest {

	@Mock
	protected ResultSet rs;

	@Mock
	protected PreparedStatement ps;

	@Mock
	protected CallableStatement cs;

	@Mock
	protected ResultSetMetaData rsmd;

	public abstract void shouldSetParameter() throws Exception;

	public abstract void shouldGetResultFromResultSetByName() throws Exception;

	public abstract void shouldGetResultNullFromResultSetByName() throws Exception;

	public abstract void shouldGetResultFromResultSetByPosition() throws Exception;

	public abstract void shouldGetResultNullFromResultSetByPosition() throws Exception;

	public abstract void shouldGetResultFromCallableStatement() throws Exception;

	public abstract void shouldGetResultNullFromCallableStatement() throws Exception;

}
