package eca.client.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eca.client.json.InstancesSerializer;
import eca.core.evaluation.EvaluationMethod;
import lombok.Data;
import weka.core.Instances;

/**
 * Experiment request transport model.
 *
 * @author Roman Batygin
 */
@Data
public class ExperimentRequestDto {

    /**
     * First name
     */
    private String firstName;

    /**
     * Email
     */
    private String email;

    /**
     * Experiment type
     */
    private ExperimentType experimentType;

    /**
     * Training data
     */
    @JsonSerialize(using = InstancesSerializer.class)
    private Instances data;

    /**
     * Evaluation method
     */
    private EvaluationMethod evaluationMethod;

}
