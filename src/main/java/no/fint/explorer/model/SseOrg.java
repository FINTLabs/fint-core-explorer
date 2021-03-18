package no.fint.explorer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SseOrg {
    private String orgId;
    private String path;
    private List<SseClient> clients = new ArrayList<>();

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SseClient {
        private String client;
        private int events;
        private String id;
        private ZonedDateTime registered;

        public void setRegistered(String registered) {
            this.registered = LocalDateTime.parse(registered, formatter).atZone(ZoneId.of("Z"));
        }
    }
}