package org.lab1.pubsub.client;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class ClientSignalHandler implements SignalHandler {

    /**
     * 注册信号
     */
    public void registerSignal(String signalName) {
        Signal signal = new Signal(signalName);
        Signal.handle(signal, this);
    }

    @Override
    public void handle(Signal sig) {
        if (sig.getName().equals("INT")) {
            // 程序关闭
            try {
                Main.stopSignal = true;
                // 退出程序
                System.exit(0);
            } catch (Exception ignore) {
            }
        }
    }
}
