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
    private final int port;

    public Server(int port) {
        this.port = port;
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
            future = bootstrap.bind("localhost", port);
            System.out.println("Run server on port " + port); // logging
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
        Server server = new Server(8080);
        try {
            server.run();
        } catch (InterruptedException ex) {
            System.out.println("Interrupted thread. Couldn't operate with channel." + ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
