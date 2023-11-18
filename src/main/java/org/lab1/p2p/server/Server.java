package org.lab1.p2p.server;

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

@SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
public class Server implements Runnable{
    private static final Logger logger = Logger.getLogger("SERVER");
    private final HashMap<String, List<String>> topics = new HashMap<>();
    @Override
    public void run() {
        var exe = Executors.newVirtualThreadPerTaskExecutor();

        var socket = Util.CreateServerSocket(6060);
        while(true) {
            try {
                //获取Socket
                Socket client = socket.accept();
                // 消息接收协程
                exe.submit(()->{
                    try (var in = new BufferedReader(new InputStreamReader(client.getInputStream()))
                            ; var out = new PrintWriter(client.getOutputStream())){
                        int dryRun = 0;
                        while(client.isConnected() && !client.isClosed()) {
                            var message = in.readLine();
                            if (message == null) {
                                Thread.sleep(10);
                                dryRun++;
                                if(dryRun >= 100)
                                    break;
                                continue;
                            }
                            var res = messageCallBack(message);
                            if(res != null) {
                                out.write(res + "\n");
                                out.flush();
                            }
                        }
                    }catch (Exception ignored) {
                        // logger.warning("Failed to receive message");
                    }/*
                    finally {

                        try {
                            client.close();
                        } catch (Exception ignored) {
                            logger.warning("Failed to close client");
                        }
                    }*/
                    // logger.warning("EXIT");
                });
            } catch (Exception e) {
                logger.warning("Failed to accept client");
            }
        }
    }
    public String messageCallBack(String message){
        synchronized (topics) {
            var res = Util.ParseMessage(message);
            if (res == null) {
                logger.warning("Failed to parse message: " + message);
                return null;
            }
            if (res.Operation == 0) {
                //System.out.println("SUB: " + res.Message);
                var tmp = res.Message.split(" ");
                for (var item : tmp) {
                    if (!topics.containsKey(item)) {
                        topics.put(item, new ArrayList<>());
                    }
                }
            } else if (res.Operation == 1) {
                //System.out.println("PUB: " + res.Message);
                var topic = res.Message.split(" ", 2);
                if (!topics.containsKey(topic[0])) {
                    logger.warning("Topic not exist : " + topic[0]);
                    return "NONE";
                }
                topics.get(topic[0]).add(topic[1]);
                return "SUCCESS";
            } else if (res.Operation == 2) {
                if (!topics.containsKey(res.Message)) {
                    logger.warning("Topic not exist : " + res.Message);
                    return "NE";
                }
                var list = topics.get(res.Message);
                String msg;
                if (list.isEmpty()) {
                    msg = "EMPTY";
                }
                else {
                    msg = "GET " + list.removeFirst();
                    //System.out.println("GET: " + res.Message + " REMAIN: " + list.size());
                }
                return msg;
            } else {
                logger.warning("Unknown operation: " + res.Operation);
            }
            return null;
        }
    }
}
