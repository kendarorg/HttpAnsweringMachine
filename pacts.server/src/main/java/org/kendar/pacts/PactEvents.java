package org.kendar.pacts;

import org.kendar.events.EventQueue;
import org.springframework.stereotype.Component;

@Component
public class PactEvents {
    public PactEvents(EventQueue eventQueue){
        eventQueue.register(this::handleViolation,PactViolation.class);
    }

    private void handleViolation(PactViolation event){

    }

}
