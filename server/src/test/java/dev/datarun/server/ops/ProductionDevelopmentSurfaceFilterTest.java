package dev.datarun.server.ops;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionDevelopmentSurfaceFilterTest {

    @Test
    void productionHidesDevelopmentAdminAndTokenSurfaces() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        ProductionDevelopmentSurfaceFilter filter =
                new ProductionDevelopmentSurfaceFilter(environment);

        MockHttpServletResponse admin = filter(filter, "/admin/dev/provision");
        MockHttpServletResponse tokens = filter(filter, "/api/actors/actor-id/tokens");

        assertThat(admin.getStatus()).isEqualTo(404);
        assertThat(tokens.getStatus()).isEqualTo(404);
    }

    @Test
    void productionDoesNotBlockRuntimeApi() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        ProductionDevelopmentSurfaceFilter filter =
                new ProductionDevelopmentSurfaceFilter(environment);

        MockHttpServletResponse response = filter(filter, "/api/auth/me");

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private MockHttpServletResponse filter(
            ProductionDevelopmentSurfaceFilter filter,
            String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
