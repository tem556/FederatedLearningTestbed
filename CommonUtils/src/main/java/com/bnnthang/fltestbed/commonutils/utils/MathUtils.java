package com.bnnthang.fltestbed.commonutils.utils;

import java.util.Random;

public class MathUtils {
    public static Double randomNormalDistribution(Double mean, Double stdDev) {
        Random r = new Random();
        return Math.abs(r.nextGaussian() * stdDev) + mean;
    }
}
