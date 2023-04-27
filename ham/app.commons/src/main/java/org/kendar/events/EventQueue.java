package org.kendar.events;

import java.util.function.Consumer;
import java.util.function.Function;

public interface EventQueue {
    <T extends Event> void register(Consumer<T> consumer, Class<T> clazz);

    <T extends Event> void registerCommand(Function<T, Object> function, Class<T> clazz);

    void handle(Event event);

    void handle(String eventType, String jsonEvent);

    <T> T execute(Event event, Class<T> clazz) throws Exception;
}
