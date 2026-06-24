package no.novari.fint.explorer.model;

import lombok.Data;
import no.fint.event.model.health.Health;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Asset {
    private String id;
    private List<ComponentStatus> components = new ArrayList<>();

    @Data
    public static class ComponentStatus {
        private String id;
        private String title;
        private ZonedDateTime lastUpdated;
        private List<SseOrg.SseClient> clients = new ArrayList<>();
        private List<Health> health = new ArrayList<>();
        private List<CacheEntry> cache = new ArrayList<>();
    }
}
