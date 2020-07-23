package com.tolpp.sevq;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class EventQueue {
    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, List<EventListener>> eventListenerMap = new HashMap<>();
    private static final Map<EventListener<?>, Executor> listenerExecutors = new HashMap<>();
    private final Executor defaultQueueExecutor;

    public EventQueue() {
        this.defaultQueueExecutor = null;
    }

    public EventQueue(Executor defaultQueueExecutor) {
        this.defaultQueueExecutor = defaultQueueExecutor;
    }

    public <T extends Event> void addEventListener(Class<T> eventType, EventListener<T> eventListener) {
        addEventListener(null, eventType, eventListener);
    }

    public <T extends Event> void addEventListener(Executor eventExecutor, Class<T> eventType, EventListener<T> eventListener) {
        if (!eventListenerMap.containsKey(eventType)) {
            eventListenerMap.put(eventType, new LinkedList<>());
        }
        if (eventExecutor != null) {
            listenerExecutors.put(eventListener, eventExecutor);
        }
        eventListenerMap.get(eventType).add(eventListener);
    }

    public <T extends Event> void send(T event) {
        Class<? extends Event> eventClass = event.getClass();
        // if no listener found, do not send this event
        if (!eventListenerMap.containsKey(eventClass)) return;

        //noinspection unchecked
        List<EventListener<T>> eventListeners = (List<EventListener<T>>) (List<?>) eventListenerMap.get(eventClass);

        for (EventListener<T> eventListener : eventListeners) {
            Executor executor = listenerExecutors.get(eventListener);
            if (executor == null) {
                executor = defaultQueueExecutor;
            }
            if (executor != null) {
                executor.execute(new EventRunnable<>(eventListener, event));
            } else {
                // run on thread that event occurred. There is no defined executor
                eventListener.onEvent(event);
            }
        }
    }

    private static class EventRunnable<T extends Event> implements Runnable {
        private final EventListener<T> eventListener;
        private final T event;

        public EventRunnable(EventListener<T> eventListener, T event) {
            this.eventListener = eventListener;
            this.event = event;
        }

        @Override
        public void run() {
            eventListener.onEvent(event);
        }
    }

}
