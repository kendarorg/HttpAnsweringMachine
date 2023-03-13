package org.kendar.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kendar.events.samples.OtherEvent;
import org.kendar.events.samples.TestEvent;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.LoggerBuilderImpl;
import org.kendar.utils.Sleeper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class EventQueueTest {
    LoggerBuilder loggerBuilder = new LoggerBuilderImpl();
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldHandleClassEvents() throws JsonProcessingException, InterruptedException {
        var results = new ArrayList<Event>();
        EventQueue target = new EventQueueImpl(loggerBuilder);

        target.register((c) -> {
            results.add(c);
        }, TestEvent.class);

        var te = new TestEvent();
        te.setString("string");
        te.setInteger(1);
        target.handle(te);
        Sleeper.sleep(500);

        assertEquals(1, results.size());
        assertEquals(mapper.writeValueAsString(te), mapper.writeValueAsString(results.get(0)));
    }

    @Test
    public void shouldHandleStringEvents() throws JsonProcessingException, InterruptedException {
        var results = new ArrayList<Event>();
        EventQueue target = new EventQueueImpl(loggerBuilder);

        target.register((c) -> {
            results.add(c);
        }, TestEvent.class);

        var te = new TestEvent();
        te.setString("string");
        te.setInteger(1);
        var tes = mapper.writeValueAsString(te);
        target.handle("TestEvent", tes);
        Sleeper.sleep(500);

        assertEquals(1, results.size());
    }

    @Test
    public void shouldIgnoreUnregisteredEvents() throws JsonProcessingException, InterruptedException {
        var results = new ArrayList<Event>();
        EventQueue target = new EventQueueImpl(loggerBuilder);

        target.register((c) -> {
            results.add(c);
        }, TestEvent.class);

        var te = new OtherEvent();
        target.handle(te);
        Sleeper.sleep(500);

        assertEquals(0, results.size());
    }

    @Test
    public void exceptionWillNotBePropagated() throws JsonProcessingException, InterruptedException {
        EventQueue target = new EventQueueImpl(loggerBuilder);

        target.register((c) -> {
            throw new RuntimeException();
        }, TestEvent.class);

        var te = new TestEvent();
        te.setString("string");
        te.setInteger(1);
        target.handle(te);
        Sleeper.sleep(500);
    }


    @Test
    public void exceptionWillNotBePropagatedToOtherHandlers() throws JsonProcessingException, InterruptedException {
        var results = new ArrayList<Event>();
        EventQueue target = new EventQueueImpl(loggerBuilder);

        target.register((c) -> {
            throw new RuntimeException();
        }, TestEvent.class);

        target.register((c) -> {
            results.add(c);
        }, TestEvent.class);

        var te = new TestEvent();
        te.setString("string");
        te.setInteger(1);
        target.handle(te);
        Sleeper.sleep(500);

        assertEquals(1, results.size());
    }
}
