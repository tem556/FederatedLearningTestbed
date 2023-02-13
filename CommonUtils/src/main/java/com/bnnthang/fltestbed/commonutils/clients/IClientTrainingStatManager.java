package com.bnnthang.fltestbed.commonutils.clients;

public interface IClientTrainingStatManager {
    void newRound();
    void setTrainingTime(Double elapsedTime);
    Double getTrainingTime(Integer round);
    Integer getRounds();
}
