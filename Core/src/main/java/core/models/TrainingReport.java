package core.models;

import lombok.Data;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.Serializable;

@Data
public class TrainingReport implements Serializable {
    protected INDArray modelParameter;
}
