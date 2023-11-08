package org.lab1.fullbroadcast.server;
import org.lab1.module.Util;

import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {
    //private static final Logger logger = Logger.getLogger("SERVER");
    public static void main(String[] args) throws InterruptedException {
        //logger.info("Server started...");
        var th = new Thread(new Server());
        th.start();
        synchronized (th)
        {
            th.wait();
        }
    }
}