package org.lab1.fullbroadcast.server;

import org.lab1.module.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@SuppressWarnings("InfiniteLoopStatement")
public class Server implements Runnable{
    private static final Logger logger = Logger.getLogger("SERVER");
    private List<Socket> clients = new ArrayList<>();
    @Override
    public void run() {
        var exe = Executors.newVirtualThreadPerTaskExecutor();

        var socket = Util.CreateServerSocket(6060);
        while(true) {
            Socket client;
            try {
                client = socket.accept();
                clients.add(client);
                int index = clients.size() - 1;
                exe.submit(()->{
                    try{
                        var in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        var out = new PrintWriter(client.getOutputStream());

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
                    }
                    finally {
                        try {
                            client.close();
                        } catch (Exception ignored) {
                        }
                        clients.remove(index);
                    }
                });
            } catch (Exception e) {
                logger.warning("Failed to accept client");
            }
        }
    }
    public void messageCallBack(String message){
        System.out.println("messageCallBack");
        System.out.println(message);
        var res = Util.ParsePortAndAddress(message);
        if(res.Operation == 1)
        {
            for (var client : clients) {
                try {
                    var out = new PrintWriter(client.getOutputStream());
                    out.write(res.Message + "\n");
                    out.flush();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
