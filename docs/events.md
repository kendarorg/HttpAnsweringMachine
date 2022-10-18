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

## Standard Events

* <u>ConfigChangedEvent</u>  when a config change is detected
  * Fields
    * name: The class name of the config that had been changed
* <u>NullCompleted</u> when a Null test is completed
* <u>PactCompleted</u> when a Pact test is completed
* <u>SSLChangedEvent</u> when the SSL hosts are changed. Forces a restart
* <u>ProxyConfigChanged</u> when the rewrites are changed. Forces their test
* <u>ScriptsModified</u> force the reloading of all Javascaript scripts
* <u>ExecuteLocalRequest</u> require the execution of a -local only- request (will not go on the internet)
* <u>ExecuteRemoteRequest</u> require the execution of a remote request