package no.fint.explorer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CacheEntry {
    private String name;
    private int size;
    private ZonedDateTime lastUpdated;

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = ZonedDateTime.parse(lastUpdated);
    }
}
