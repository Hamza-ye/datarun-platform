package dev.datarun.ship1.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.datarun.ship1.scope.ScopeResolver;
import dev.datarun.ship1.sync.ActorAuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Delivers the Ship-1 config package to an authenticated CHV device.
 *
 * <p>Returns the single deployer shape ({@code household_observation/v1}), the single activity
 * ({@code household_observation}), and the list of villages reachable by this CHV's current
 * assignments. Villages are scope-filtered so a CHV only sees pickers for their assigned areas.
 */
@RestController
@RequestMapping("/api/sync")
public class ConfigController {

    private final VillageRepository villages;
    private final ScopeResolver scopes;
    private final ObjectMapper mapper;

    public ConfigController(VillageRepository villages, ScopeResolver scopes, ObjectMapper mapper) {
        this.villages = villages;
        this.scopes = scopes;
        this.mapper = mapper;
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig(HttpServletRequest request) throws Exception {
        UUID actorId = (UUID) request.getAttribute(ActorAuthInterceptor.ATTR_ACTOR_ID);
        Set<UUID> actorScopes = scopes.activeGeographicScopes(actorId.toString(), OffsetDateTime.now());
        List<VillageRepository.Village> visibleVillages =
                villages.findByIds(new ArrayList<>(actorScopes));

        ObjectNode root = mapper.createObjectNode();
        root.put("version", 1);

        ArrayNode activities = root.putArray("activities");
        ObjectNode activity = activities.addObject();
        activity.put("name", "household_observation");

        ArrayNode shapes = root.putArray("shapes");
        ObjectNode shape = shapes.addObject();
        shape.put("shape_ref", "household_observation/v1");
        shape.set("schema", loadShapeJson("household_observation"));

        ArrayNode villageArr = root.putArray("villages");
        for (VillageRepository.Village v : visibleVillages) {
            ObjectNode vn = villageArr.addObject();
            vn.put("id", v.id().toString());
            vn.put("district_name", v.districtName());
            vn.put("name", v.name());
        }
        return ResponseEntity.ok(root);
    }

    private JsonNode loadShapeJson(String shapeName) throws Exception {
        try (InputStream in = new ClassPathResource(
                "schemas/shapes/" + shapeName + ".schema.json").getInputStream()) {
            return mapper.readTree(in);
        }
    }
}
