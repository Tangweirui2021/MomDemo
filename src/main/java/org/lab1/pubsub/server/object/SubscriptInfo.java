package org.lab1.pubsub.server.object;

import java.util.UUID;

public class SubscriptInfo {
    public String topic;
    public UUID id;
    public SubscriptInfo(String topic, UUID id) {
        this.topic = topic;
        this.id = id;
    }
}
