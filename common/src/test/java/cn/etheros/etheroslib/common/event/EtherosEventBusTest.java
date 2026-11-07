package cn.etheros.etheroslib.common.event;

import cn.etheros.etherosframework.event.BusEvent;
import cn.etheros.etherosframework.event.EventBus;
import cn.etheros.etherosframework.event.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EtherosEventBusTest {

    private EtherosEventBus bus;

    @BeforeEach
    void setUp() {
        bus = new EtherosEventBus();
    }

    @Test
    void callsListenersInPriorityOrder() {
        List<String> order = new ArrayList<>();
        bus.subscribe("test", event -> order.add("high"), Priority.HIGH);
        bus.subscribe("test", event -> order.add("low"), Priority.LOW);
        bus.subscribe("test", event -> order.add("medium"), Priority.MEDIUM);

        bus.post("test", new BusEvent());

        assertEquals(Arrays.asList("low", "medium", "high"), order);
    }

    @Test
    void keepsRegistrationOrderWithinSamePriority() {
        List<String> order = new ArrayList<>();
        bus.subscribe("test", event -> order.add("first"), Priority.MEDIUM);
        bus.subscribe("test", event -> order.add("second"), Priority.MEDIUM);
        bus.subscribe("test", event -> order.add("third"), Priority.MEDIUM);

        bus.post("test", new BusEvent());

        assertEquals(Arrays.asList("first", "second", "third"), order);
    }

    @Test
    void passesEventPayloadToListener() {
        AtomicReference<Object> seen = new AtomicReference<>();
        bus.subscribe("test", event -> seen.set(event.getData().get("player")), Priority.MEDIUM);

        bus.post("test", new BusEvent().setData(Collections.singletonMap("player", "Steve")));

        assertEquals("Steve", seen.get());
    }

    @Test
    void unsubscribeStopsDeliveryAndIsIdempotent() {
        List<String> order = new ArrayList<>();
        EventBus.Subscription subscription =
                bus.subscribe("test", event -> order.add("called"), Priority.MEDIUM);

        subscription.unsubscribe();
        subscription.unsubscribe();
        bus.post("test", new BusEvent());

        assertTrue(order.isEmpty());
    }

    @Test
    void multiEventSubscriptionDeliversToAllAndRemovesFromAll() {
        List<String> order = new ArrayList<>();
        EventBus.Subscription subscription =
                bus.subscribe(Arrays.asList("a", "b"), event -> order.add("called"), Priority.MEDIUM);

        bus.post("a", new BusEvent());
        bus.post("b", new BusEvent());
        assertEquals(2, order.size());

        subscription.unsubscribe();
        bus.post("a", new BusEvent());
        bus.post("b", new BusEvent());
        assertEquals(2, order.size());
    }

    @Test
    void duplicateEventTypesDeliverOnce() {
        List<String> order = new ArrayList<>();
        bus.subscribe(Arrays.asList("a", "a"), event -> order.add("called"), Priority.MEDIUM);

        bus.post("a", new BusEvent());

        assertEquals(1, order.size());
    }

    @Test
    void cancelStopsLaterListeners() {
        List<String> order = new ArrayList<>();
        bus.subscribe("test", event -> event.setCancelled(true), Priority.LOW);
        bus.subscribe("test", event -> order.add("high"), Priority.HIGH);

        bus.post("test", new BusEvent());

        assertTrue(order.isEmpty());
    }

    @Test
    void listenerSubscribedDuringDispatchRunsFromNextPost() {
        List<String> order = new ArrayList<>();
        bus.subscribe("test", event -> {
            order.add("first");
            bus.subscribe("test", late -> order.add("late"), Priority.HIGH);
        }, Priority.LOW);

        bus.post("test", new BusEvent());
        assertEquals(Collections.singletonList("first"), order);

        bus.post("test", new BusEvent());
        assertEquals(Arrays.asList("first", "first", "late"), order);
    }

    @Test
    void listenerUnsubscribedDuringDispatchFinishesCurrentPost() {
        List<String> order = new ArrayList<>();
        final EventBus.Subscription[] holder = new EventBus.Subscription[1];
        bus.subscribe("test", event -> {
            order.add("first");
            holder[0].unsubscribe();
        }, Priority.LOW);
        holder[0] = bus.subscribe("test", event -> order.add("second"), Priority.LOW);

        bus.post("test", new BusEvent());
        assertEquals(Arrays.asList("first", "second"), order);

        order.clear();
        bus.post("test", new BusEvent());
        assertEquals(Collections.singletonList("first"), order);
    }

    @Test
    void listenerExceptionAbortsDispatch() {
        List<String> order = new ArrayList<>();
        bus.subscribe("test", event -> {
            throw new IllegalStateException("boom");
        }, Priority.LOW);
        bus.subscribe("test", event -> order.add("later"), Priority.HIGH);

        assertThrows(IllegalStateException.class, () -> bus.post("test", new BusEvent()));
        assertTrue(order.isEmpty());
    }

    @Test
    void postToUnknownEventTypeDoesNothing() {
        bus.post("unknown", new BusEvent());
    }
}
