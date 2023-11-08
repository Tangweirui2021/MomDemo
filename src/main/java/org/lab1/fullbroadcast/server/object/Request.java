package org.lab1.fullbroadcast.server.object;

public class Request {
    public int Operation;
    public String Message;

    public Request(int i, String s) {
        Operation = i;
        Message = s;
    }
}
