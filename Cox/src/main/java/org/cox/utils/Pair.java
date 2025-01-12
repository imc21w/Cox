package org.cox.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Pair<T, V> {
    private final T first;

    @Setter
    private V second;

    private Pair(T first, V second) {
        this.first = first;
        this.second = second;
    }

    public static <T,V> Pair<T, V> of(T first, V second) {
        return new Pair<>(first, second);
    }
}
