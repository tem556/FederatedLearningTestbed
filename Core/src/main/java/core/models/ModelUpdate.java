package core.models;

import lombok.Data;
import org.nd4j.linalg.api.ndarray.INDArray;

@Data
public class ModelUpdate {
    protected INDArray newModelParameter;
}
