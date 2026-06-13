package dev.datarun.server.ops;

import com.fasterxml.jackson.databind.JsonNode;
import dev.datarun.server.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureObservability
class ObservabilityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void exposesLivenessReadinessAndPrometheusButNotOtherActuatorEndpoints() {
        ResponseEntity<JsonNode> liveness =
                rest.getForEntity("/actuator/health/liveness", JsonNode.class);
        ResponseEntity<JsonNode> readiness =
                rest.getForEntity("/actuator/health/readiness", JsonNode.class);
        ResponseEntity<String> metrics =
                rest.getForEntity("/actuator/prometheus", String.class);
        ResponseEntity<String> environment =
                rest.getForEntity("/actuator/env", String.class);

        assertThat(liveness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(liveness.getBody().path("status").asText()).isEqualTo("UP");
        assertThat(readiness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readiness.getBody().path("status").asText()).isEqualTo("UP");
        assertThat(metrics.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metrics.getBody()).contains("jvm_");
        assertThat(environment.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void requestLoggingUsesSafeRequestIdWithoutReflectingInvalidInput() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(StructuredRequestLoggingFilter.REQUEST_ID_HEADER, "../../unsafe");
        ResponseEntity<JsonNode> accepted = rest.exchange(
                "/actuator/health/liveness",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class);

        String requestId = accepted.getHeaders().getFirst(
                StructuredRequestLoggingFilter.REQUEST_ID_HEADER);
        assertThat(requestId).isNotBlank();
        assertThat(requestId).isNotEqualTo("../../unsafe");
    }
}
