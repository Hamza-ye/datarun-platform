package dev.datarun.server.contracts;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract test: the two envelope schema files must agree byte-for-byte.
 *
 * <p>{@code contracts/envelope.schema.json} is the language-neutral source of truth shared with
 * mobile and any future client. {@code server/src/main/resources/envelope.schema.json} is the
 * server-bundled copy consumed by the JSON Schema validator at runtime. Nothing structural
 * prevents the two from diverging silently — this test is the structural enforcement.
 *
 * <p>Phase 3e audit finding B4 / {@code docs/flagged-positions.md} FP-003. The Phase 1/2
 * envelope-type-vocabulary drift was present in both copies because they were edited together,
 * but an agent editing only one file would not have broken any existing test. This test fails
 * the build in that scenario.
 *
 * <p>Normalization: trailing-newline differences are tolerated (editors add them inconsistently);
 * all other bytes must match.
 */
class EnvelopeSchemaParityTest {

    @Test
    void bothEnvelopeSchemaCopiesAgreeByteForByte() throws Exception {
        Path contractsCopy = Paths.get("..", "contracts", "envelope.schema.json").toAbsolutePath().normalize();
        Path serverCopy = Paths.get("src", "main", "resources", "envelope.schema.json").toAbsolutePath().normalize();

        assertTrue(Files.exists(contractsCopy), "Missing: " + contractsCopy);
        assertTrue(Files.exists(serverCopy), "Missing: " + serverCopy);

        String a = normalize(Files.readString(contractsCopy));
        String b = normalize(Files.readString(serverCopy));

        assertEquals(a, b,
                "Envelope schema copies must stay identical. "
                        + "Edit both in a single change, or fold one into the other. "
                        + "See docs/flagged-positions.md FP-003 and ADR-002 Addendum.");
    }

    private static String normalize(String s) {
        // Tolerate trailing-newline differences only.
        while (s.endsWith("\n") || s.endsWith("\r")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
