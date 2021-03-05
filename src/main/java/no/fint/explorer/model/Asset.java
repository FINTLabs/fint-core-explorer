package no.fint.explorer.model;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Asset {
    private String id;
    private ZonedDateTime lastUpdated;
    private List<Component> components = new ArrayList<>();

    @Data
    public static class Component {
        private String path;
        private List<SseOrg.SseClient> clients = new ArrayList<>();
    }
}
