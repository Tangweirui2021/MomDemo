package org.lab1.fullbroadcast.client;

import org.lab1.module.object.PublishInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@SuppressWarnings("BusyWait")
public class Main {
    public static boolean stopSignal = false;
    private static final Logger logger = Logger.getLogger("CLIENT");
    private static final List<PublishInfo> _pub = new ArrayList<>();
    private static final List<String> _sub = new ArrayList<>();
    private static boolean _repeat = false;
    public static void main(String[] args) {
        logger.info("Client started...");
        ClientSignalHandler mqKillHandler = new ClientSignalHandler();
        mqKillHandler.registerSignal("INT");

        getEnv();
        var exe = Executors.newVirtualThreadPerTaskExecutor();
        try (var socket = new Socket("127.0.0.1", 6060)) {
            socket.setKeepAlive(true);
            socket.setSoTimeout(1000);

            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new PrintWriter(socket.getOutputStream());
            // 消息接收协程
            if (!_sub.isEmpty()) {
                logger.info("Subscribed: " + String.join(",", _sub));
                exe.submit(()-> {
                    while (!stopSignal) {
                        String read = null;
                        try {
                            read = in.readLine();
                        } catch (SocketTimeoutException ignored) {
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (read == null) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            continue;
                        }
                        //判断是否以_sub中的字符串开头
                        boolean flag = false;
                        var readSplit = read.split(" ", 2);
                        for (var item : _sub) {
                            if (readSplit[0].equals(item)) {
                                flag = true;
                                break;
                            }
                        }
                        if (flag) {
                            logger.info("Received message: " + read);
                        }else{
                            logger.info("Received message: " + read + " (ignored)");
                        }
                    }
                });
            }
            // 消息发送协程
            if (!_pub.isEmpty()) {
                logger.info("Publish: " + String.join(",", _pub.stream().map(x -> x.message).toArray(String[]::new)));
                exe.submit(()-> {
                    do{
                        for (var item : _pub) {
                            try {
                                Thread.sleep(item.delay * 1000L);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            out.write("PUB " + item.message + "\n");
                            out.flush();
                            logger.info("Message sent");
                        }
                    }while (_repeat && !stopSignal);
                });
            }
            while (!stopSignal) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Thread.sleep(1000);
            // 关闭socket
            in.close();
            out.close();
            socket.close();
            // 关闭线程池
            exe.close();
        } catch (Exception e) {
            logger.warning("Failed to send message");
        }
    }

    public static void getEnv() {
        var pub = System.getenv("PUB");
        //示例: PUB=A 12:0,B 23:2,C 34:1 代表发布A 12,B 23,C 34三条信息，A立即发送，B在A后延迟2秒发送，C在B后延迟1秒发送
        if(pub != null) {
            var pubTmp = pub.split(",");
            for (var item : pubTmp) {
                var tmp = item.split(":", 2);
                if (tmp.length != 2) {
                    logger.warning("Invalid PUB env");
                    System.exit(-1);
                }
                try {
                    _pub.add(new PublishInfo(tmp[0], Integer.parseInt(tmp[1])));
                } catch (Exception e) {
                    logger.warning("Invalid PUB env");
                    System.exit(-1);
                }
            }
        }
        var repeat = System.getenv("REPEAT");
        if (repeat != null && repeat.equalsIgnoreCase("true")) {
            _repeat = true;
        }
        var sub = System.getenv("SUB");
        //示例: SUB=A,B,C 代表订阅A,B,C三类信息
        if(sub != null) {
            var subTmp = sub.split(",");
            _sub.addAll(Arrays.asList(subTmp));
        }
    }
}
