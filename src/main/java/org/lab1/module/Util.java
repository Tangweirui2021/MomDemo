package org.lab1.module;

import org.lab1.fullbroadcast.server.object.Request;

import java.net.ServerSocket;
import java.util.logging.Logger;

public class Util {
    private static final Logger logger = Logger.getLogger("UTIL");
    public static ServerSocket CreateServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            logger.warning("Failed to create server socket");
            System.exit(1);
        }
        return serverSocket;
    }
    public static Request ParseMessage(String str) {
        var request = str.split(" ", 2);
        switch (request[0]) {
            case "PUB":
                return new Request(1, request[1]);
            default:
                return null;
        }
    }
}
