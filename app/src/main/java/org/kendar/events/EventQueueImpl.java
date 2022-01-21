package org.kendar.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class EventQueueImpl implements EventQueue {
    private final Logger logger;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    public EventQueueImpl(LoggerBuilder loggerBuilder){
        this.logger = loggerBuilder.build(EventQueue.class);
    }
    private final HashMap<String,List<Consumer<Event>>> eventHandlers = new HashMap<>();
    private final HashMap<String,Class> conversions = new HashMap<>();


    @Override
    public <T extends Event> void register(Consumer<T> consumer, Class<T> clazz) {
        var eventName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        conversions.put(eventName,clazz);
        var realConsumer = new Consumer<Event>(){
            @Override
            public void accept(Event event) {
                consumer.accept((T)event);
            }
        };
        if(!eventHandlers.containsKey(eventName)){
            eventHandlers.put(eventName,new ArrayList<>());
        }
        eventHandlers.get(eventName).add(realConsumer);
    }

    @Override
    public void handle(Event event) {
        var eventName = event.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        if(!eventHandlers.containsKey(eventName)) return;
        var handlers = eventHandlers.get(eventName);
        executorService.submit(()->{
            for(var i=0;i<handlers.size();i++){
                var handler = handlers.get(i);
                try{
                    handler.accept(event);
                }catch (Exception ex){
                    logger.error("Error executing Event "+eventName,ex);
                }
            }

        });
    }

    @Override
    public void handle(String eventType, String jsonEvent) {
        eventType = eventType.toLowerCase(Locale.ROOT);
        if(!conversions.containsKey(eventType))return;
        var clazz = conversions.get(eventType);
        try {
            var event = (Event)mapper.readValue(jsonEvent,clazz);
            handle(event);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing Event "+eventType+" with body: "+jsonEvent,e);
        }
    }
}
