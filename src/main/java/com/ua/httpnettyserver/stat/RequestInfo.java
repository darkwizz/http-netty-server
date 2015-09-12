package com.ua.httpnettyserver.stat;

import java.util.Date;

/**
 * Created by ARTUR on 05.09.2015.
 */
public class RequestInfo {
    private int requestsCount;
    private Date lastRequestTime;

    public RequestInfo() {
        requestsCount = 1;
        lastRequestTime = new Date(System.currentTimeMillis());
    }

    public void increaseRequestCount() {
        requestsCount++;
    }

    public int getRequestsCount() {
        return requestsCount;
    }

    public void updateLastRequestTime() {
        lastRequestTime = new Date(System.currentTimeMillis());
    }

    public Date getLastRequestTime() {
        return lastRequestTime;
    }
}
