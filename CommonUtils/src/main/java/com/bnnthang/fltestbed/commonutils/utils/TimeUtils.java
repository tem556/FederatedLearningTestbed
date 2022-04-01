package com.bnnthang.fltestbed.commonutils.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {
    public static Double millisecondsBetween(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return 1000.0 * duration.getSeconds() + (double) duration.getNano() / 1000000.0;
    }
}
