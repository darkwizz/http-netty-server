package com.ua.httpnettyserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;

/**
 * Created by ARTUR on 04.09.2015.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext context, Object message) {
        try {
            if (message instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) message;
                QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
                String path = decoder.path();
                System.out.println("Path: " + path); // logging
                switch (path) {
                    case "/helloworld":
                        handleHelloWorld(context);
                        break;
                    default:
                        sendResponse(context, "<h1>EHLO there. Unknown path</h1>", HttpResponseStatus.OK);
                        context.close();
                        break;
                }
            } else {
                System.out.println("New message");
            }
        } finally {
            ReferenceCountUtil.release(message);
        }
    }

    private void sendResponse(ChannelHandlerContext context, String responseText, HttpResponseStatus status) {
        String responseBody = HtmlResponseCreator.create().
                fillBody(responseText).toString();
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        context.writeAndFlush(response);
    }

    private void handleHelloWorld(final ChannelHandlerContext context) {
        context.executor().schedule(new Runnable() {
            @Override
            public void run() {
                sendResponse(context, "Hello World", HttpResponseStatus.OK);
                context.close();
            }
        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        System.out.println("Some fail");
        context.close();
    }
}
