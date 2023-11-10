package org.lab1.pubsub.server;

import org.lab1.pubsub.server.object.PublishInfoServer;
import org.lab1.pubsub.server.object.SubscriptInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.lab1.module.Util.*;

@SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
public class Server implements Runnable{
    private static final Logger logger = Logger.getLogger("SERVER");
    private final HashMap<String, HashMap<UUID, Socket>> clients = new HashMap<>();
    private final HashMap<String, List<PublishInfoServer>> topics = new HashMap<>();
    @Override
    public void run() {
        var exe = Executors.newVirtualThreadPerTaskExecutor();

        var socket = CreateServerSocket(6060);
        while(true) {
            try {
                //获取Socket
                Socket client = socket.accept();
                // 消息接收协程
                exe.submit(()->{
                    var subs = new ArrayList<SubscriptInfo>();
                    try (var in = new BufferedReader(new InputStreamReader(client.getInputStream()))){
                        while(client.isConnected() && !client.isClosed()) {
                            var message = in.readLine();
                            if (message == null) {
                                Thread.sleep(10);
                                continue;
                            }
                            var res = messageCallBack(message, client);
                            if(res != null) {
                                subs.addAll(res);
                            }
                        }
                    }catch (Exception ignored) {
                        logger.warning("Failed to receive message");
                    }
                    finally {
                        try {
                            client.close();
                            for (var item : subs) {
                                clients.get(item.topic).remove(item.id);
                            }
                        } catch (Exception ignored) {
                            logger.warning("Failed to close client");
                        }
                    }
                });
                // 信息失效检查
                exe.submit(()->{
                    while(true){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        checkValid();
                    }
                });
            } catch (Exception e) {
                logger.warning("Failed to accept client");
            }
        }
    }
    public List<SubscriptInfo> messageCallBack(String message, Socket client) throws IOException {
        synchronized (topics) {
            var res = ParseMessage(message);
            if (res == null) {
                logger.warning("Failed to parse message: " + message);
                return null;
            }
            if (res.Operation == 0) {
                System.out.println("SUB: " + res.Message);
                var tmp = res.Message.split(" ");
                var sub = new ArrayList<SubscriptInfo>();
                for (var item : tmp) {
                    if (!topics.containsKey(item)) {
                        topics.put(item, new ArrayList<>());
                        clients.put(item, new HashMap<>());
                        var id = org.lab1.pubsub.server.Util.getUuid();
                        clients.get(item).put(id, client);
                        sub.add(new SubscriptInfo(item, id));
                    }else{
                        var id = org.lab1.pubsub.server.Util.getUuid();
                        clients.get(item).put(id, client);
                        sub.add(new SubscriptInfo(item, id));
                        checkValid();
                        for (var info : topics.get(item)) {
                            var out = new PrintWriter(client.getOutputStream());
                            out.write(info.message + "\n");
                            out.flush();
                        }
                    }
                }
                return sub;
            } else if (res.Operation == 1) {
                System.out.println("PUB: " + res.Message);
                var topic = res.Message.split(" ", 3);
                if (!topics.containsKey(topic[0])) {
                    logger.warning("Topic not exist : " + topic[0]);
                }
                PublishInfoServer info;
                try{
                    info = new PublishInfoServer(topic[1], Integer.parseInt(topic[2]));
                }catch (Exception e) {
                    logger.warning("Failed to parse message: " + message);
                    return null;
                }
                topics.get(topic[0]).add(info);
                for (var item : clients.get(topic[0]).values()) {
                    var out = new PrintWriter(item.getOutputStream());
                    out.write(info.message + "\n");
                    out.flush();
                }
            } else {
                logger.warning("Unknown operation: " + res.Operation);
            }
            return null;
        }
    }
    public void checkValid() {
        synchronized (topics) {
            for (var item : topics.values()) {
                var remove = new ArrayList<PublishInfoServer>();
                var now = new Date();
                for (var info : item) {
                    if (info.validTime.before(now)) {
                        remove.add(info);
                    }
                }
                item.removeAll(remove);
            }
        }
    }
}
