package cn.etheros.etheroslib.common.event;

import cn.etheros.etherosframework.event.BusEvent;
import cn.etheros.etherosframework.event.EventBus;
import cn.etheros.etherosframework.event.Priority;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 事件总线默认实现
 *
 * <p>监听器按 {@link Priority} 升序调用，同优先级按注册顺序
 * <p>事件置为已取消后停止调用后续监听器
 * <p>监听器在 {@link #post} 的调用线程同步执行，异常直接抛给调用方并中断本次投递
 * <p>平台模块以子类 + {@code @GlobalService} 的方式接入 DI，生命周期结束时调用 {@link #shutdown()}
 *
 * @author 西博卡兹
 * @since 1.0.0
 */
public class EtherosEventBus implements EventBus {

    // 优先级升序；List.sort 稳定，同优先级保持注册顺序
    private static final Comparator<Registration> BY_PRIORITY =
            Comparator.comparingInt(registration -> registration.priority.ordinal());

    private final Map<String, CopyOnWriteArrayList<Registration>> registrations =
            new ConcurrentHashMap<>();

    @Override
    public Subscription subscribe(String eventType, Consumer<BusEvent> listener, Priority priority) {
        return subscribe(Collections.singletonList(eventType), listener, priority);
    }

    @Override
    public Subscription subscribe(List<String> eventTypes, Consumer<BusEvent> listener, Priority priority) {
        Objects.requireNonNull(eventTypes, "eventTypes");
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(priority, "priority");
        Registration registration = new Registration(listener, priority, eventTypes);
        for (String eventType : registration.eventTypes) {
            CopyOnWriteArrayList<Registration> listeners =
                    registrations.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<Registration>());
            listeners.add(registration);
            listeners.sort(BY_PRIORITY);
        }
        return registration;
    }

    @Override
    public void post(String eventType, BusEvent event) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(event, "event");
        CopyOnWriteArrayList<Registration> listeners = registrations.get(eventType);
        if (listeners == null) {
            return;
        }
        for (Registration registration : listeners) {
            if (event.isCancelled()) {
                return;
            }
            registration.listener.accept(event);
        }
    }

    /**
     * 释放全部监听器
     */
    public void shutdown() {
        registrations.clear();
    }

    private final class Registration implements Subscription {

        private final Consumer<BusEvent> listener;
        private final Priority priority;
        private final Set<String> eventTypes;

        private Registration(Consumer<BusEvent> listener, Priority priority, List<String> eventTypes) {
            this.listener = listener;
            this.priority = priority;
            Set<String> types = new LinkedHashSet<String>();
            for (String eventType : eventTypes) {
                types.add(Objects.requireNonNull(eventType, "eventType"));
            }
            this.eventTypes = Collections.unmodifiableSet(types);
        }

        @Override
        public void unsubscribe() {
            for (String eventType : eventTypes) {
                CopyOnWriteArrayList<Registration> listeners = registrations.get(eventType);
                if (listeners != null) {
                    listeners.remove(this);
                }
            }
        }

        @Override
        public String toString() {
            return "Subscription{eventTypes=" + eventTypes + ", priority=" + priority + "}";
        }
    }
}
