package org.lab1.module.object;

public class PublishInfoPS {
    public PublishInfoPS(String message, int validity, int delay) {
        this.message = message;
        this.validity = validity;
        this.delay = delay;
    }
    public String message;
    public int validity;
    public int delay;
}
