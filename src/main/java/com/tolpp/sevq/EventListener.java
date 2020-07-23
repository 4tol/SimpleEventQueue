package com.tolpp.sevq;

public interface EventListener<T extends Event> {

    void onEvent(T event);
}
