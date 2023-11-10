package org.lab1.pubsub.server.object;

import java.util.Date;

public class PublishInfoServer {
    public String message;
    public Date validTime;
    public PublishInfoServer(String message, int validTime) {
        this.message = message;
        this.validTime = new Date(System.currentTimeMillis() + validTime * 1000L);
    }
}
