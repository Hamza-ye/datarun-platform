package dev.datarun.server.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Validates events against the envelope JSON Schema (contracts/envelope.schema.json).
 * The schema is the source of truth — this class simply applies it.
 */
@Component
public class EnvelopeValidator {

    private final ObjectMapper objectMapper;
    private JsonSchema schema;

    public EnvelopeValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadSchema() throws IOException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        try (InputStream is = new ClassPathResource("envelope.schema.json").getInputStream()) {
            schema = factory.getSchema(is);
        }
    }

    /**
     * Validate a JSON node against the envelope schema.
     * Returns empty list if valid, or list of violation messages.
     */
    public List<String> validate(JsonNode eventNode) {
        Set<ValidationMessage> errors = schema.validate(eventNode);
        return errors.stream()
                .map(ValidationMessage::getMessage)
                .toList();
    }
}
