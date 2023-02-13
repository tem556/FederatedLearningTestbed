package com.bnnthang.fltestbed.commonutils.clients;

public interface IClientNetworkStatManager {
    void increaseBytes(Long x);
    void increaseCommTime(Double x);
    void newRound();
    Long getBytes(Integer round);
    Double getCommTime(Integer round);
    Integer getRounds();
}
