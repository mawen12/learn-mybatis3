package com.mawen.learn.mybatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.mybatis.annotations.Insert;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/31
 */
public enum JdbcType {

	ARRAY(Types.ARRAY),
	BIT(Types.BIT),
	TINYINT(Types.TINYINT),
	SMALLINT(Types.SMALLINT),
	INTEGER(Types.INTEGER),
	BIGINT(Types.BIGINT),
	FLOAT(Types.FLOAT),
	REAL(Types.REAL),
	DOUBLE(Types.DOUBLE),
	NUMERIC(Types.NUMERIC),
	DECIMAL(Types.DECIMAL),
	CHAR(Types.CHAR),
	VARCHAR(Types.VARCHAR),
	LONGVARCHAR(Types.LONGVARCHAR),
	DATE(Types.DATE),
	TIME(Types.TIME),
	TIMESTAMP(Types.TIMESTAMP),
	BINARY(Types.BINARY),
	VARBINARY(Types.VARBINARY),
	LONGVARBINARY(Types.LONGVARBINARY),
	NULL(Types.NULL),
	OTHER(Types.OTHER),
	BLOB(Types.BLOB),
	CLOB(Types.CLOB),
	BOOLEAN(Types.BOOLEAN),
	CURSOR(-10),
	UNDEFINED(Integer.MIN_VALUE + 1000),
	NVARCHAR(Types.NVARCHAR),
	NCHAR(Types.NCHAR),
	NCLOB(Types.NCLOB),
	STRUCT(Types.STRUCT),
	JAVA_OBJECT(Types.JAVA_OBJECT),
	DISTINCT(Types.DISTINCT),
	REF(Types.REF),
	DATALINK(Types.DATALINK),
	ROWID(Types.ROWID),
	LONGNVARCHAR(Types.LONGNVARCHAR),
	SQLXML(Types.SQLXML),
	DATETIMEOFFSET(-155),
	TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE),
	TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE),;


	public final int TYPE_CODE;

	JdbcType(int TYPE_CODE) {
		this.TYPE_CODE = TYPE_CODE;
	}

	private static Map<Integer, JdbcType> codeLookup = new HashMap<>();

	static {
		for (JdbcType type : JdbcType.values()) {
			codeLookup.put(type.TYPE_CODE, type);
		}
	}

	public static JdbcType forCode(int code) {
		return codeLookup.get(code);
	}
}
