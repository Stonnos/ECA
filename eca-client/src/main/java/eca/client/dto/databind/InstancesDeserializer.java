package eca.client.dto.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.SerializationUtils;
import weka.core.Instances;

import java.io.IOException;
import java.util.Base64;

/**
 * Instances json deserializer.
 *
 * @author Roman Batygin
 */
public class InstancesDeserializer extends JsonDeserializer<Instances> {

    @Override
    public Instances deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        byte[] dataBytes = Base64.getDecoder().decode(jsonNode.textValue());
        return (Instances) SerializationUtils.deserialize(dataBytes);
    }
}
