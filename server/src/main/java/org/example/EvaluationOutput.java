package org.example;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.callbacks.EvaluationCallback;
import org.nd4j.evaluation.IEvaluation;

import java.io.File;

public class EvaluationOutput implements EvaluationCallback {
    @Override
    public void call(EvaluativeListener evaluativeListener, Model model, long l, IEvaluation[] iEvaluations) {
        // TODO: save to an external file
        for (IEvaluation i : iEvaluations) {
            System.out.println("eval out: " + i.toJson());
        }
    }
}
