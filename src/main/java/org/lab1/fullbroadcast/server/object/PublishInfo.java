package org.lab1.fullbroadcast.server.object;

public class PublishInfo {
    public PublishInfo(String message, int delay) {
        this.message = message;
        this.delay = delay;
    }
    public String message;
    public int delay;
}
