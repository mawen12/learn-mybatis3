package com.mawen.learn.mybatis.datasource.pooled;

import com.mawen.learn.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

	public PooledDataSourceFactory() {
		this.dataSource = new PooledDataSource();
	}
}
