package no.fint.explorer.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Asset {
    private String asset;
    private List<Component> components = new ArrayList<>();

    @Data
    public static class Component {
        private String path;
        private List<SseOrg.SseClient> clients = new ArrayList<>();
    }
}
