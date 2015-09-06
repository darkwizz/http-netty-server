package com.ua.httpnettyserver.stat;

import java.util.Date;

/**
 * Created by ARTUR on 06.09.2015.
 */
public class ConnectionInfo {
    private String ipSource;
    private String url;
    private Date timestamp;
    private int sentBytes;
    private int receivedBytes;
    private double sendingSpeed;

    public ConnectionInfo(String ipSource, String url, Date timestamp, int sentBytes, int receivedBytes,
                          double sendingSpeed) {
        this.ipSource = ipSource;
        this.url = url;
        this.timestamp = timestamp;
        this.sentBytes = sentBytes;
        this.receivedBytes = receivedBytes;
        this.sendingSpeed = sendingSpeed;
    }

    public String getIpSource() {
        return ipSource;
    }

    public String getUrl() {
        return url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getSentBytes() {
        return sentBytes;
    }

    public int getReceivedBytes() {
        return receivedBytes;
    }

    public double getSendingSpeed() {
        return sendingSpeed;
    }
}
