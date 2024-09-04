package com.mawen.learn.mybatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public interface ParameterHandler {

	Object getParameterObject();

	void setParameters(PreparedStatement ps) throws SQLException;
}
