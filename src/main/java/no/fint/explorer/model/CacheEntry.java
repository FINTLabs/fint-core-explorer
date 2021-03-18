package no.fint.explorer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CacheEntry {
    @JsonProperty("class")
    private String clazz;

    private ZonedDateTime lastUpdated;
    private int size;

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = ZonedDateTime.parse(lastUpdated);
    }
}
