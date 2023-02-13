package com.bnnthang.fltestbed.commonutils.clients;

import java.util.ArrayList;
import java.util.List;

public class ClientTrainingStatManager implements IClientTrainingStatManager {
    /**
     * Store the time taken for training per round.
     */
    private List<Double> trainingTimeByRound;

    public ClientTrainingStatManager() {
        trainingTimeByRound = new ArrayList<>();
    }

    @Override
    public void newRound() {
        trainingTimeByRound.add(0.0);
    }

    @Override
    public void setTrainingTime(Double elapsedTime) {
        trainingTimeByRound.set(trainingTimeByRound.size() - 1, elapsedTime);
    }

    @Override
    public Double getTrainingTime(Integer round) {
        return trainingTimeByRound.get(round);
    }

    @Override
    public Integer getRounds() {
        return trainingTimeByRound.size();
    }
}
