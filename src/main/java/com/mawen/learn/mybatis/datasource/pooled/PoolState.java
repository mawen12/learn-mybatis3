package com.mawen.learn.mybatis.datasource.pooled;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/4
 */
public class PoolState {

	protected PooledDataSource dataSource;

	protected final List<PooledConnection> idleConnections = new ArrayList<>();
	protected final List<PooledConnection> activeConnections = new ArrayList<>();
	protected long requestCount = 0;
	protected long accumulatedRequestTime = 0;
	protected long accumulatedCheckoutTime = 0;
	protected long claimedOverdueConnectionCount = 0;
	protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
	protected long accumulatedWaitTime = 0;
	protected long hadToWaitCount = 0;
	protected long badConnectionCount = 0;

	public PoolState(PooledDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public synchronized long getRequestCount() {
		return requestCount;
	}

	public synchronized long getAverageRequestTime() {
		return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
	}

	public synchronized long getAverageWaitTime() {
		return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
	}

	public synchronized long getHadToWaitCount() {
		return hadToWaitCount;
	}

	public synchronized long getBadConnectionCount() {
		return badConnectionCount;
	}

	public synchronized long getClaimedOverdueConnectionCount() {
		return claimedOverdueConnectionCount;
	}

	public synchronized long getAverageOverdueCheckoutTime() {
		return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
	}

	public synchronized long getAverageCheckoutTime() {
		return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
	}

	public synchronized int getIdleConnectionCount() {
		return idleConnections.size();
	}

	public synchronized int getActiveConnectionCount() {
		return activeConnections.size();
	}

	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n===CONFIGURATION==============================================");
		builder.append("\n jdbcDriver                     ").append(dataSource.getDriver());
		builder.append("\n jdbcUrl                        ").append(dataSource.getUrl());
		builder.append("\n jdbcUsername                   ").append(dataSource.getUsername());
		builder.append("\n jdbcPassword                   ").append(dataSource.getPassword() == null ? "NULL" : "************");
		builder.append("\n poolMaxActiveConnections       ").append(dataSource.poolMaximumActiveConnections);
		builder.append("\n poolMaxIdleConnections         ").append(dataSource.poolMaximumIdleConnections);
		builder.append("\n poolMaxCheckoutTime            ").append(dataSource.poolMaximumCheckoutTime);
		builder.append("\n poolTimeToWait                 ").append(dataSource.poolTimeToWait);
		builder.append("\n poolPingEnabled                ").append(dataSource.poolPingEnabled);
		builder.append("\n poolPingQuery                  ").append(dataSource.poolPingQuery);
		builder.append("\n poolPingConnectionsNotUsedFor  ").append(dataSource.poolPingConnectionsNotUsedFor);
		builder.append("\n ---STATUS-----------------------------------------------------");
		builder.append("\n activeConnections              ").append(getActiveConnectionCount());
		builder.append("\n idleConnections                ").append(getIdleConnectionCount());
		builder.append("\n requestCount                   ").append(getRequestCount());
		builder.append("\n averageRequestTime             ").append(getAverageRequestTime());
		builder.append("\n averageCheckoutTime            ").append(getAverageCheckoutTime());
		builder.append("\n claimedOverdue                 ").append(getClaimedOverdueConnectionCount());
		builder.append("\n averageOverdueCheckoutTime     ").append(getAverageOverdueCheckoutTime());
		builder.append("\n hadToWait                      ").append(getHadToWaitCount());
		builder.append("\n averageWaitTime                ").append(getAverageWaitTime());
		builder.append("\n badConnectionCount             ").append(getBadConnectionCount());
		builder.append("\n===============================================================");
		return builder.toString();
	}
}
