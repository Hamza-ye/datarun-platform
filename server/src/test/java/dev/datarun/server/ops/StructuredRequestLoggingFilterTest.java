package dev.datarun.server.ops;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.datarun.server.authorization.ActorTokenInterceptor;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class StructuredRequestLoggingFilterTest {

    @Test
    void emitsStructuredSecretSafeRequestFields() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(
                StructuredRequestLoggingFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            UUID actorId = UUID.randomUUID();
            MockHttpServletRequest request =
                    new MockHttpServletRequest("POST", "/api/sync/push");
            request.setQueryString("access_token=query-secret");
            request.addHeader("Authorization", "Bearer header-secret");
            request.setAttribute(ActorTokenInterceptor.ACTOR_ID_ATTR, actorId);
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(503);

            new StructuredRequestLoggingFilter().doFilter(
                    request, response, new MockFilterChain());

            assertThat(appender.list).hasSize(1);
            ILoggingEvent event = appender.list.get(0);
            Map<String, String> fields = event.getKeyValuePairs().stream()
                    .collect(Collectors.toMap(
                            pair -> pair.key,
                            pair -> String.valueOf(pair.value)));
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).isEqualTo("http_request");
            assertThat(fields).containsEntry("http_method", "POST")
                    .containsEntry("http_path", "/api/sync/push")
                    .containsEntry("http_status", "503")
                    .containsEntry("actor_id", actorId.toString())
                    .containsKey("duration_ms");
            assertThat(event.toString())
                    .doesNotContain("query-secret")
                    .doesNotContain("header-secret")
                    .doesNotContain("Authorization");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
