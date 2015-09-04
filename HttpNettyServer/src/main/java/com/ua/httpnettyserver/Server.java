package com.ua.httpnettyserver;

/**
 * Created by ARTUR on 04.09.2015.
 */
public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        System.out.println("Run server on port " + port);
    }

    public static void main(String[] argv) {
        Server server = new Server(80);
        server.run();
    }
}
