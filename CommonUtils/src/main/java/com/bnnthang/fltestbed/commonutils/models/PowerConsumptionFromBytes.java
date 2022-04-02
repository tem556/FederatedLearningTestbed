package com.bnnthang.fltestbed.commonutils.models;

import com.bnnthang.fltestbed.commonutils.utils.MathUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
public class PowerConsumptionFromBytes implements Serializable {
    @Getter
    private Double powerConsumption;

    @Setter
    private Double avgPowerPerBytes = null;

    public PowerConsumptionFromBytes() {
        powerConsumption = 0.0;
    }

    public void increasePowerConsumption(Long bytes) {
        powerConsumption += bytes * MathUtils.randomNormalDistribution(avgPowerPerBytes, 9.25 * avgPowerPerBytes);
    }
}
