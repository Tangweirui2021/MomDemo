package org.lab1.fullbroadcast.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;
import org.lab1.module.Util;

public class Main {
    private static final Logger logger = Logger.getLogger("CLIENT");
    public static void main(String[] args) {
        logger.info("Client started...");
        try (var socket = new Socket("127.0.0.1", 6060)) {
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new PrintWriter(socket.getOutputStream());
            var message = "Hello from client\n";
            out.write(message);
            out.flush();
            logger.info("Message sent");
            while (true) {
                var read = in.readLine();
                if (read == null) {
                    //noinspection BusyWait
                    Thread.sleep(10);
                    continue;
                }
                logger.info("Received message: " + read);
            }
        } catch (Exception e) {
            logger.warning("Failed to send message");
        }

    }
}
