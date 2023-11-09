package org.lab1.module;

import org.lab1.module.object.Request;

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
        return switch (request[0]) {
            case "SUB" -> // 对于选择广播式，代表创建一个新的话题
                    new Request(0, request[1]);
            case "PUB" ->// 对于选择广播式和全广播式，都代表发布一个新的消息
                    new Request(1, request[1]);
            case "GET" ->// 对于选择全广播式，代表获取话题下的消息
                    new Request(2, request[1]);
            default -> null;
        };
    }
}
