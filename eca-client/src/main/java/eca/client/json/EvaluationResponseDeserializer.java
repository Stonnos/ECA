package eca.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import eca.client.dto.EvaluationResponse;
import eca.client.dto.TechnicalStatus;
import eca.core.evaluation.EvaluationResults;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Base64;

import static eca.client.dictionary.JsonFieldsDictionary.ERROR_MESSAGE;
import static eca.client.dictionary.JsonFieldsDictionary.EVALUATION_RESULTS;
import static eca.client.dictionary.JsonFieldsDictionary.STATUS;

/**
 * Evaluation response json deserializer.
 *
 * @author Roman Batygin
 */
public class EvaluationResponseDeserializer extends JsonDeserializer<EvaluationResponse> {

    @Override
    public EvaluationResponse deserialize(JsonParser parser,
                                          DeserializationContext context) throws IOException {
        JsonNode jsonNode = parser.getCodec().readTree(parser);
        EvaluationResponse classificationResultsDto = new EvaluationResponse();
        JsonNode classifierNode = jsonNode.get(EVALUATION_RESULTS);
        if (!classifierNode.isNull()) {
            byte[] bytes = Base64.getDecoder().decode(classifierNode.textValue());
            EvaluationResults evaluationResults = (EvaluationResults) SerializationUtils.deserialize(bytes);
            classificationResultsDto.setEvaluationResults(evaluationResults);
        }
        classificationResultsDto.setStatus(TechnicalStatus.valueOf(jsonNode.get(STATUS).textValue()));
        classificationResultsDto.setErrorMessage(jsonNode.get(ERROR_MESSAGE).textValue());
        return classificationResultsDto;
    }
}