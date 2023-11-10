package org.lab1.pubsub.server;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class Main {
    public static void main(String[] args) throws InterruptedException {
        var th = new Thread(new Server());
        th.start();
        synchronized (th)
        {
            th.wait();
        }
    }
}