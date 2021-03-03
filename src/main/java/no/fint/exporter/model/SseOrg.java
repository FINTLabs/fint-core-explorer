package no.fint.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SseOrg {
    private List<SseClient> clients = new ArrayList<>();
    private String orgId;
    private String path;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SseClient {
        private String client;
        private int events;
        private String id;
        private String registered;
    }
}