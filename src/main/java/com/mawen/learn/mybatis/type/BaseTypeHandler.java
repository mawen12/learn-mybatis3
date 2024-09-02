package com.mawen.learn.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mawen.learn.mybatis.executor.result.ResultMapException;
import com.mawen.learn.mybatis.session.Configuration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {

	protected Configuration configuration;

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
		if (parameter == null) {
			if (jdbcType == null) {
				throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
			}

			try {
				ps.setNull(i, jdbcType.TYPE_CODE);
			}
			catch (SQLException e) {
				throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + ". "
				                        + "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property."
				                        + "Cause: " + e, e);
			}
		}
		else {
			try {
				setNonNullParameter(ps, i, parameter, jdbcType);
			}
			catch (SQLException e) {
				throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + ". "
				                        + "Try settings a different JdbcType for this parameter or a different configuration property."
				                        + "Cause: " + e, e);
			}
		}
	}

	@Override
	public T getResult(ResultSet rs, String columnName) throws SQLException {
		try {
			return getNullableResult(rs, columnName);
		}
		catch (Exception e) {
			throw new ResultMapException("Error attempting to get column '" + columnName + "' from request set. Cause: " + e, e);
		}
	}

	@Override
	public T getResult(ResultSet rs, int columnIndex) throws SQLException {
		try {
			return getNullableResult(rs,columnIndex);
		}
		catch (SQLException e) {
			throw new ResultMapException("Error attempting to get column #" + columnIndex + " from request set. Cause: " + e, e);
		}
	}

	@Override
	public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
		try {
			return getNullableResult(cs, columnIndex);
		}
		catch (SQLException e) {
			throw new ResultMapException("Error attempting to get column #" + columnIndex + " from callable statement. Cause: " + e, e);
		}
	}

	public abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

	public abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

	public abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;

	public abstract T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;

}
