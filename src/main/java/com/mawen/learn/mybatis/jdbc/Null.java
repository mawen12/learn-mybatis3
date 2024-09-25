package com.mawen.learn.mybatis.jdbc;

import java.sql.JDBCType;

import com.mawen.learn.mybatis.type.BigDecimalTypeHandler;
import com.mawen.learn.mybatis.type.BlobTypeHandler;
import com.mawen.learn.mybatis.type.BooleanTypeHandler;
import com.mawen.learn.mybatis.type.ByteArrayTypeHandler;
import com.mawen.learn.mybatis.type.ByteTypeHandler;
import com.mawen.learn.mybatis.type.DateOnlyTypeHandler;
import com.mawen.learn.mybatis.type.DateTypeHandler;
import com.mawen.learn.mybatis.type.DoubleTypeHandler;
import com.mawen.learn.mybatis.type.FloatTypeHandler;
import com.mawen.learn.mybatis.type.IntegerTypeHandler;
import com.mawen.learn.mybatis.type.JdbcType;
import com.mawen.learn.mybatis.type.LongTypeHandler;
import com.mawen.learn.mybatis.type.ObjectTypeHandler;
import com.mawen.learn.mybatis.type.ShortTypeHandler;
import com.mawen.learn.mybatis.type.SqlDateTypeHandler;
import com.mawen.learn.mybatis.type.SqlTimeTypeHandler;
import com.mawen.learn.mybatis.type.SqlTimestampTypeHandler;
import com.mawen.learn.mybatis.type.StringTypeHandler;
import com.mawen.learn.mybatis.type.TimeOnlyTypeHandler;
import com.mawen.learn.mybatis.type.TypeHandler;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
public enum Null {
	BOOLEAN(new BooleanTypeHandler(), JdbcType.BOOLEAN),

	BYTE(new ByteTypeHandler(), JdbcType.TINYINT),
	SHORT(new ShortTypeHandler(), JdbcType.SMALLINT),
	INTEGER(new IntegerTypeHandler(), JdbcType.INTEGER),
	LONG(new LongTypeHandler(), JdbcType.BIGINT),
	FLOAT(new FloatTypeHandler(), JdbcType.FLOAT),
	DOUBLE(new DoubleTypeHandler(), JdbcType.DOUBLE),
	BIGDECIMAL(new BigDecimalTypeHandler(), JdbcType.DECIMAL),

	STRING(new StringTypeHandler(), JdbcType.VARCHAR),
	CLOB(new BlobTypeHandler(), JdbcType.CLOB),
	LONGVARCHAR(new BlobTypeHandler(), JdbcType.LONGNVARCHAR),

	BYTEARRAY(new ByteArrayTypeHandler(), JdbcType.LONGVARBINARY),
	BLOB(new BlobTypeHandler(), JdbcType.BLOB),
	LONGVARBINARY(new BlobTypeHandler(), JdbcType.LONGVARBINARY),

	OBJECT(new ObjectTypeHandler(), JdbcType.OTHER),
	OTHER(new ObjectTypeHandler(), JdbcType.OTHER),
	TIMESTAMP(new DateTypeHandler(), JdbcType.TIMESTAMP),
	DATE(new DateOnlyTypeHandler(), JdbcType.DATE),
	TIME(new TimeOnlyTypeHandler(), JdbcType.TIME),
	SQLTIMESTAMP(new SqlTimestampTypeHandler(), JdbcType.TIMESTAMP),
	SQLDATE(new SqlDateTypeHandler(), JdbcType.DATE),
	SQLTIME(new SqlTimeTypeHandler(), JdbcType.TIME),
	;

	private final TypeHandler<?> typeHandler;
	private final JdbcType jdbcType;

	Null(TypeHandler<?> typeHandler, JdbcType jdbcType) {
		this.typeHandler = typeHandler;
		this.jdbcType = jdbcType;
	}

	public TypeHandler<?> getTypeHandler() {
		return typeHandler;
	}

	public JdbcType getJdbcType() {
		return jdbcType;
	}
}
