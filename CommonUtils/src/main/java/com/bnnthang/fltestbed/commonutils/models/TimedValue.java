package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;

@Data
public class TimedValue<T> {
    /**
     * The main value.
     */
    private T value;

    /**
     * Elasped time (in milliseconds).
     */
    private Double elapsedTime;

    public TimedValue(T value, Double elapsedTime) {
        this.value = value;
        this.elapsedTime = elapsedTime;
    }
}
