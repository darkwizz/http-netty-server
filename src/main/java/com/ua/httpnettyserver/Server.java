package com.ua.httpnettyserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.IOException;

import static java.lang.Thread.sleep;

/**
 * Created by ARTUR on 04.09.2015.
 */
public class Server {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "80";

    private final int port;
    private final String host;

    public Server(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void run() throws InterruptedException, IOException {
        EventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture future = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group); // set one event loop group both for boss thread and worker threads
            bootstrap.channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast("decoder", new HttpRequestDecoder())
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("handler", new ServerHandler());
                        }
                    })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            future = bootstrap.bind(host, port);
            System.out.println("Run server on " + host + ":" + port); // logging
            //System.in.read();
            while(true) {
                sleep(60);
            }
        } finally {
            if (future != null) {
                future.channel().closeFuture().sync();
            }
            System.out.println("Parent channel is closed"); // logging
            group.shutdownGracefully();
            System.out.println("Server has been shut down"); // logging
        }
    }

    public static void main(String[] argv) {
        String host = System.getenv("OPENSHIFT_INTERNAL_IP");
        if (host == null) {
            host = System.getenv("OPENSHIFT_DIY_IP");
            host = host == null ? DEFAULT_HOST : host;
        }
        System.out.println("Host: " + host); // logging
        String strPort = System.getenv("OPENSHIFT_INTERNAL_PORT");
        if (strPort == null) {
            strPort = System.getenv("OPENSHIFT_DIY_PORT");
            strPort = strPort == null ? DEFAULT_PORT : strPort;
        }
        System.out.println("Port: " + strPort); // logging
        int port = Integer.parseInt(strPort);
        Server server = new Server(port, host);
        try {
            server.run();
        } catch (InterruptedException ex) {
            System.out.println("Interrupted thread. Couldn't operate with channel." + ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
