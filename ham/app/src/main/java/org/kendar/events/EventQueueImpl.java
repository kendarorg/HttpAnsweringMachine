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
import java.util.function.Function;

@Component
public class EventQueueImpl implements EventQueue {
    private final Logger logger;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final HashMap<String, List<Consumer<Event>>> eventHandlers = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<String, Class> conversions = new HashMap<>();
    private final HashMap<String, Function<Event, Object>> commandHandlers = new HashMap<>();

    public EventQueueImpl(LoggerBuilder loggerBuilder) {
        this.logger = loggerBuilder.build(EventQueue.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void register(Consumer<T> consumer, Class<T> clazz) {
        var eventName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        conversions.put(eventName, clazz);
        var realConsumer = new Consumer<Event>() {
            @Override
            public void accept(Event event) {
                consumer.accept((T) event);
            }
        };
        if (!eventHandlers.containsKey(eventName)) {
            eventHandlers.put(eventName, new ArrayList<>());
        }
        eventHandlers.get(eventName).add(realConsumer);
    }

    @Override
    public <T extends Event> void registerCommand(Function<T, Object> function, Class<T> clazz) {
        var eventName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        conversions.put(eventName, clazz);
        var realConsumer = new Function<Event, Object>() {
            @Override
            public Object apply(Event event) {
                return function.apply((T) event);
            }
        };
        if (commandHandlers.containsKey(eventName)) {
            throw new RuntimeException("Duplicate event " + eventName);
        }
        commandHandlers.put(eventName, realConsumer);
    }

    @Override
    public void handle(Event event) {
        var eventName = event.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        if (!eventHandlers.containsKey(eventName) &&
                !commandHandlers.containsKey(eventName)) return;
        var handlers = eventHandlers.get(eventName);
        var handler = commandHandlers.get(eventName);
        if (handlers != null) {
            executorService.submit(() -> {
                for (var i = 0; i < handlers.size(); i++) {
                    var subHandler = handlers.get(i);
                    try {
                        subHandler.accept(event);
                    } catch (Exception ex) {
                        logger.error("Error executing Event " + eventName, ex);
                    }
                }

            });
        } else if (handler != null) {
            executorService.submit(() -> {

                try {
                    handler.apply(event);
                } catch (Exception ex) {
                    logger.error("Error executing Event " + eventName, ex);
                }

            });
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(String eventType, String jsonEvent) {
        eventType = eventType.toLowerCase(Locale.ROOT);
        if (!conversions.containsKey(eventType)) return;
        var clazz = conversions.get(eventType);
        try {
            var event = (Event) mapper.readValue(jsonEvent, clazz);
            handle(event);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing Event " + eventType + " with body: " + jsonEvent, e);
        }
    }

    @Override
    public <T> T execute(Event event, Class<T> clazz) throws Exception {
        var eventName = event.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        if (!commandHandlers.containsKey(eventName)) return null;
        var handler = commandHandlers.get(eventName);

        return (T) handler.apply(event);
    }
}
