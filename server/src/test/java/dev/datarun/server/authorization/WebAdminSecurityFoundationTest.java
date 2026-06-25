package dev.datarun.server.authorization;

import dev.datarun.server.ops.ProductionDevelopmentSurfaceFilter;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class WebAdminSecurityFoundationTest {

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        mvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .addFilters(springSecurityFilterChain,
                        new ProductionDevelopmentSurfaceFilter(environment))
                .build();
    }

    @Test
    void springSecurityDoesNotReplaceBearerApiOrCsrfDecisions() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("WWW-Authenticate"));

        mvc.perform(post("/api/sync/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void springSecurityAllowsProductionDevelopmentSurfaceContainmentToReturn404()
            throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(get("/admin/dev/provision"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(get("/admin/config"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(post("/api/actors/{actorId}/tokens", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(get("/api/subjects"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(get("/api/subjects/{subjectId}/events", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Controller
    private static final class ProbeController {

        @GetMapping("/api/auth/me")
        @ResponseBody
        String authMe() {
            return "{}";
        }

        @PostMapping("/api/sync/pull")
        @ResponseBody
        String syncPull() {
            return "{}";
        }

        @GetMapping({"/admin", "/admin/dev/provision", "/admin/config"})
        @ResponseBody
        String admin() {
            return "development";
        }

        @PostMapping("/api/actors/{actorId}/tokens")
        @ResponseBody
        String actorTokens(@PathVariable UUID actorId) {
            return actorId.toString();
        }

        @GetMapping("/api/subjects")
        @ResponseBody
        String subjects() {
            return "{\"subjects\":[]}";
        }

        @GetMapping("/api/subjects/{subjectId}/events")
        @ResponseBody
        String subjectEvents(@PathVariable UUID subjectId) {
            return "{\"subject_id\":\"" + subjectId + "\",\"events\":[]}";
        }
    }
}
