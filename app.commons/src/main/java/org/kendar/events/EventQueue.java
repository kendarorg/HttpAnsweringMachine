package org.kendar.events;

import java.util.function.Consumer;

public interface EventQueue {
      <T extends Event> void register(Consumer<T> consumer,Class<T> clazz);
      void handle(Event event);
    void handle(String eventType, String jsonEvent);
}
