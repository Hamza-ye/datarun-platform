package dev.datarun.ship1.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validates event envelopes against {@code contracts/envelope.schema.json} (bundled at build).
 * Called on every inbound push BEFORE persistence (ADR-001 §S3, Ship-1 §6.3 step "validates envelope").
 */
@Component
public class EnvelopeValidator {

    private JsonSchema schema;

    @PostConstruct
    void init() throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        try (InputStream in = new ClassPathResource("schemas/envelope.schema.json").getInputStream()) {
            this.schema = factory.getSchema(in);
        }
    }

    /** Returns an empty list on success, or human-readable violation messages otherwise. */
    public List<String> validate(JsonNode envelope) {
        Set<ValidationMessage> errors = schema.validate(envelope);
        List<String> out = new ArrayList<>(errors.size());
        for (ValidationMessage m : errors) {
            out.add(m.getMessage());
        }
        return out;
    }
}
