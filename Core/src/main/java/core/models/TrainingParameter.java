package core.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class TrainingParameter implements Serializable {
    public int batchSize;
    public int epochs;
}
