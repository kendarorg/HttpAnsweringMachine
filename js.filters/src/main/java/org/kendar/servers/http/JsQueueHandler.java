package org.kendar.servers.http;

import org.kendar.events.EventQueue;

public class JsQueueHandler {
    private EventQueue queue;

    public JsQueueHandler(EventQueue queue){

        this.queue = queue;
    }

    public void handle(String eventType,String jsonEvent){
        queue.handle(eventType,jsonEvent);
    }
}
