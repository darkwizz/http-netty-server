package com.ua.httpnettyserver;

import com.ua.httpnettyserver.responseelements.Table;
import com.ua.httpnettyserver.stat.ConnectionInfo;
import com.ua.httpnettyserver.stat.RequestInfo;
import com.ua.httpnettyserver.stat.ServerStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;

/**
 * Created by ARTUR on 04.09.2015.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private String ipSource = "";
    private int sentBytes = 0;
    private int receivedBytes = 0;
    private String url = "";
    private Date timestamp = null;
    private long sendingStart = 0;
    private long sendingEnd = 0;
    private ConnectionInfo info;

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) {
        try {
//            if (message instanceof ByteBuf) {
//                System.out.println("Bytebuf"); // test
//            }
            if (message instanceof HttpRequest) {
                // fill server stat
                ServerStatus.increaseRequestCount();
                addRequestInfo(context);

                HttpRequest request = (HttpRequest) message;
                receivedBytes = request.toString().getBytes("UTF-8").length;
                url = request.getUri();
                timestamp = new Date(System.currentTimeMillis());

                QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
                String path = decoder.path();
                System.out.println("Path: " + path); // logging
                switch (path) {
                    case "/":
                        // start page
                        sendResponse(context, "<h1>HELLO! It's a temporary start page.</h1>", HttpResponseStatus.OK,
                                new WritingChannelListener());
                        context.close();
                        break;
                    case "/helloworld":
                        handleHelloWorld(context);
                        break;
                    case "/redirect":
                        if (!decoder.parameters().containsKey("url")) {
                            sendResponse(context, "<p>No redirect url specified</p>", HttpResponseStatus.BAD_REQUEST,
                                    new WritingChannelListener());
                            context.close();
                            return;
                        }
                        String redirectUrl = decoder.parameters().get("url").get(0);
                        handleRedirect(context, redirectUrl);
                        break;
                    case "/status":
                        handleStatus(context);
                        break;
                    case "/stop":
                        handleStop(context);
                        break;
                    default:
                        sendResponse(context, "<h1>EHLO there. Unknown path</h1>", HttpResponseStatus.OK,
                                new WritingChannelListener());
                        context.close();
                        break;
                }
            } else {
                System.out.println("New message"); // logging
                //context.close();
            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Some error with encoding - " + ex); // logging
        } finally {
            ReferenceCountUtil.release(message);
        }
    }

    private void handleStop(ChannelHandlerContext context) {
        try {
            context.executor().parent().shutdownGracefully().await();
        } catch (InterruptedException e) {
            context.close();
        }
    }

    private void addRequestInfo(ChannelHandlerContext context) {
        Channel channel = context.channel();
        if (channel.remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            ipSource = address.getAddress().toString();
            ServerStatus.increaseRequestCount(address.getAddress());
        }
    }

    private void handleStatus(ChannelHandlerContext context) {
        List<String> statItems = new ArrayList<String>();
        statItems.add("Summary requests => " + ServerStatus.getRequestsCount());
        statItems.add("Unique requests count => " + getUniqueRequestsCount());
        statItems.add("Summary running connection => " + ServerStatus.getRunningConnectionsCount());
        statItems.add("Redirects table => \n{redirectsTable}");
        statItems.add("IP requests table => \n{ipRequestsTable}");
        statItems.add("Last 16 connections log => \n{connectionsTable}");

        HtmlResponseCreator creator = HtmlResponseCreator.create();
        String statList = creator.createList(statItems);
        String redirectTable = creator.createTable(ServerStatus.getRedirectingStat());
        String ipRequestsTable = getIpRequestsTable(creator);
        String connectionsInfoTable = getConnectionsInfoTable(creator);

        statList = statList.replace("{redirectsTable}", redirectTable);
        statList = statList.replace("{ipRequestsTable}", ipRequestsTable);
        statList = statList.replace("{connectionsTable}", connectionsInfoTable);

        sendResponse(context, creator.fillBody(statList).toString(), HttpResponseStatus.OK, new WritingChannelListener());
    }

    private String getConnectionsInfoTable(HtmlResponseCreator creator) {
        List<String> headers = new ArrayList<>();
        headers.add("IP source");
        headers.add("URL");
        headers.add("timestamp");
        headers.add("sent bytes");
        headers.add("received bytes");
        headers.add("sending speed(bytes/sec)");
        Table infoTable = creator.createTable(headers);
        for (ConnectionInfo info : ServerStatus.getConnectionInfoLog()) {
            List<String> row = new ArrayList<>();
            row.add(info.getIpSource());
            row.add(info.getUrl());
            row.add(info.getTimestamp().toString());
            row.add(info.getSentBytes() + "");
            row.add(info.getReceivedBytes() + "");
            row.add(info.getSendingSpeed() + "");
            infoTable.addRow(row);
        }
        return infoTable.toString();
    }

    private String getIpRequestsTable(HtmlResponseCreator creator) {
        List<String> headers = new ArrayList<>();
        headers.add("IP");
        headers.add("Requests count");
        headers.add("Last request time");
        Table requestsTable = creator.createTable(headers);
        for (Map.Entry<InetAddress, RequestInfo> entry : ServerStatus.getRequestInfoStat().entrySet()) {
            List<String> row = new ArrayList<>();
            row.add(entry.getKey().toString());
            row.add(entry.getValue().getRequestsCount() + "");
            row.add(entry.getValue().getLastRequestTime().toString());
            requestsTable.addRow(row);
        }
        return requestsTable.toString();
    }

    private int getUniqueRequestsCount() {
        int requestsCount = 0;
        for (Map.Entry<InetAddress, RequestInfo> entry : ServerStatus.getRequestInfoStat().entrySet()) {
            if (entry.getValue().getRequestsCount() == 1) {
                requestsCount++;
            }
        }
        return requestsCount;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext context) {
        System.out.println("Added"); // logging
        // this handler is added once per one channel
        // and one channel means one connection
        // so until this channel won't be closed
        // this channel and connection will be running
        ServerStatus.increaseRunningConnectionsCount();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext context) {
        System.out.println("Removed"); // logging
        // handler will be removed when channel will be closed
        ServerStatus.decreaseRunningConnectionsCount();
    }

    private void sendResponse(ChannelHandlerContext context, String responseText, HttpResponseStatus status,
                              ChannelFutureListener listener) {
        String responseBody = HtmlResponseCreator.create().
                fillBody(responseText).toString();
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        sentBytes = response.content().readableBytes();
        sendingStart = System.currentTimeMillis();
        context.writeAndFlush(response).addListener(listener);
    }

    private void handleHelloWorld(final ChannelHandlerContext context) {
        context.executor().schedule(new Runnable() {
            @Override
            public void run() {
                sendResponse(context, "Hello World", HttpResponseStatus.OK, new WritingChannelListener());
            }
        }, 10, TimeUnit.SECONDS);
    }

    private void handleRedirect(ChannelHandlerContext context, String redirectUrl) {
        if (!redirectUrl.contains("http://")) {
            redirectUrl = "http://" + redirectUrl;
        }
        ServerStatus.increaseUrlRedirectingCount(redirectUrl);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        //response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(HttpHeaders.Names.LOCATION, redirectUrl);
        sentBytes = response.content().readableBytes();
        sendingStart = System.currentTimeMillis();
        context.writeAndFlush(response).addListener(new WritingChannelListener());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        System.out.println("Some fail - " + cause);
        context.close();
    }

    private class WritingChannelListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture future) {
            sendingEnd = System.currentTimeMillis();
            double sendingTime = (sendingEnd - sendingStart) / 1000.0;
            double sendingSpeed = sentBytes / sendingTime;
            info = new ConnectionInfo(ipSource, url, timestamp, sentBytes, receivedBytes, sendingSpeed);
            ServerStatus.addConnectionInfo(info);
            future.channel().close();
        }
    }
}
