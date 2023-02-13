package com.bnnthang.fltestbed.commonutils.clients;

import java.util.ArrayList;
import java.util.List;

public class ClientNetworkStatManager implements IClientNetworkStatManager {
    /**
     * Current round number.
     */
    private Integer currentRound;

    /**
     * Store the number of bytes sent per round.
     */
    private List<Long> bytesSentByRound;

    /**
     * Store the time elapsed for networking per round.
     */
    private List<Double> timeElapsedByRound;

    public ClientNetworkStatManager() {
        currentRound = 0;
        bytesSentByRound = new ArrayList<>();
        timeElapsedByRound = new ArrayList<>();
    }

    @Override
    public void increaseBytes(Long x) {
        bytesSentByRound.set(currentRound - 1, bytesSentByRound.get(currentRound - 1) + x);
    }

    @Override
    public void increaseCommTime(Double x) {
        timeElapsedByRound.set(currentRound - 1, timeElapsedByRound.get(currentRound - 1) + x);
    }

    @Override
    public void newRound() {
        currentRound = 0;
        bytesSentByRound.add(0L);
        timeElapsedByRound.add(0.0);
    }

    @Override
    public Long getBytes(Integer round) {
        return bytesSentByRound.get(round);
    }

    @Override
    public Double getCommTime(Integer round) {
        return timeElapsedByRound.get(round);
    }

    @Override
    public Integer getRounds() {
        return currentRound;
    }
}
