package org.lab1.fullbroadcast.server;

import org.lab1.module.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class Server implements Runnable{
    private static final Logger logger = Logger.getLogger("SERVER");
    private final HashMap<Integer, Socket> clients = new HashMap<>();
    private int clientId = 0;
    @Override
    public void run() {
        var exe = Executors.newVirtualThreadPerTaskExecutor();

        var socket = Util.CreateServerSocket(6060);
        while(true) {
            try {
                //获取Socket
                Socket client = socket.accept();
                //添加到客户端列表
                var id = ++clientId;
                clients.put(id, client);
                // 消息接收协程
                exe.submit(()->{
                    try (var in = new BufferedReader(new InputStreamReader(client.getInputStream()))){
                        while(client.isConnected() && !client.isClosed()) {
                            var message = in.readLine();
                            if (message == null) {
                                //noinspection BusyWait
                                Thread.sleep(10);
                                continue;
                            }
                            messageCallBack(message);
                        }
                    }catch (Exception ignored) {
                        logger.warning("Failed to receive message");
                    }
                    finally {
                        try {
                            client.close();
                        } catch (Exception ignored) {
                            logger.warning("Failed to close client");
                        }
                        clients.remove(id);
                    }
                });
            } catch (Exception e) {
                logger.warning("Failed to accept client");
            }
        }
    }
    public void messageCallBack(String message){
        var res = Util.ParseMessage(message);
        if (res == null) {
            logger.warning("Failed to parse message: " + message);
            return;
        }
        if(res.Operation == 1)
        {
            System.out.println("PUB: " + res.Message);
            for (var item : clients.keySet()) {
                try {
                    var client = clients.get(item);
                    if(!client.isConnected() || client.isClosed()){
                        clients.remove(item);
                        continue;
                    }
                    var out = new PrintWriter(client.getOutputStream());
                    out.write(res.Message + "\n");
                    out.flush();
                    //System.out.println("Message sent to " + item + "@" + client.getLocalSocketAddress());
                    //输出当前客户端信息
                    System.out.println("Message sent to " + item + "@" + client.getRemoteSocketAddress());
                } catch (Exception ignored) {
                    logger.warning("Failed to send message");
                }
            }
        }
    }
}
