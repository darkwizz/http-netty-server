package com.ua.httpnettyserver.stat;

import java.net.InetAddress;
import java.util.*;

/**
 * Created by ARTUR on 05.09.2015.
 */
public class ServerStatus {
    private static final int LOG_SIZE = 16;

    private static Map<String, Integer> urlInfoTable =
            new HashMap<String, Integer>();
    private static int requestsCount = 0;
    private static int runningConnectionsCount = 0;
    private static Map<InetAddress, RequestInfo> requestInfoTable =
            new HashMap<>();
    private static Queue<ConnectionInfo> connectionInfoLog = new LinkedList<>();

    private ServerStatus() {}

    public static Map<String, Integer> getRedirectingStat() {
        return urlInfoTable;
    }

    public static void increaseUrlRedirectingCount(String url) {
        Integer count = urlInfoTable.containsKey(url) ? urlInfoTable.get(url) + 1 : 1;
        urlInfoTable.put(url, count);
    }

    public static void clearUrlCountingTable() {
        urlInfoTable.clear();
    }

    public static void increaseRequestCount() {
        requestsCount++;
    }

    public static int getRequestsCount() {
        return requestsCount;
    }

    public static void increaseRequestCount(InetAddress address) {
        if (!requestInfoTable.containsKey(address)) {
            requestInfoTable.put(address, new RequestInfo());
        } else {
            RequestInfo info = requestInfoTable.get(address);
            info.increaseRequestCount();
            info.updateLastRequestTime();
        }
    }

    public static Map<InetAddress, RequestInfo> getRequestInfoStat() {
        return requestInfoTable;
    }

    public static void clearRequestInfoTable() {
        requestInfoTable.clear();
    }

    public static void increaseRunningConnectionsCount() {
        runningConnectionsCount++;
    }

    public static void decreaseRunningConnectionsCount() {
        runningConnectionsCount--;
    }

    public static int getRunningConnectionsCount() {
        return runningConnectionsCount;
    }

    public static void addConnectionInfo(ConnectionInfo info) {
        connectionInfoLog.add(info);
        if (connectionInfoLog.size() > LOG_SIZE) {
            connectionInfoLog.poll();
        }
    }

    public static Collection<ConnectionInfo> getConnectionInfoLog() {
        return connectionInfoLog;
    }
}
