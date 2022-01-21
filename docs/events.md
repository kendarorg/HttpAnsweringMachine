## Events

Some action is able to produce synchronous events.

The events must inherit from "org.kendar.events.Event"

To register for a certain event it'enough to add a dependency on the EventQueue interface
and register the event handler:

<pre>
@Component
public class PactEvents {
    public PactEvents(EventQueue eventQueue){
        eventQueue.register((e)->handleViolation(e),PactViolation.class);
    }
    
    private void handleViolation(PactViolation event){
        //Do something
    }
</pre>