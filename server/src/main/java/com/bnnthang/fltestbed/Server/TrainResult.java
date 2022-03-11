package com.bnnthang.fltestbed.Server;

import org.nd4j.common.util.SerializationUtils;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class TrainResult implements Serializable {
    public List<byte[]> layerParams;
    public long trainingTime;

    public TrainResult(List<INDArray> layerParams, long trainingTime) {
        this.layerParams = layerParams.stream().map(x -> {
            return SerializationUtils.toByteArray(x);
        }).collect(Collectors.toList());
        this.trainingTime = trainingTime;
    }
}
