package no.fint.explorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.health.Health;
import no.fint.explorer.repository.ClusterRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ConsumerService {
    private final ClusterRepository clusterRepository;

    private final static String HEALTH_ENDPOINT = "/admin/health";
    private final static String CONSUMER_ROLE = "fint.role=consumer";
    private final static String CONSUMER_PREFIX = "consumer-";

    public ConsumerService(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    public List<V1Service> getConsumers() {
        return clusterRepository.getNamespacedServices(CONSUMER_ROLE);
    }

    public Optional<V1Service> getConsumer(String id) {
        return clusterRepository.getNamespacedService(CONSUMER_PREFIX + id);
    }

    public List<Health> getHealth(V1Service v1Service, String assetId) {
        return clusterRepository.getApiResponse(v1Service, HEALTH_ENDPOINT, assetId)
                .map(ApiResponse::getData)
                .map(this::getValue)
                .map(Event::getData)
                .orElseGet(Collections::emptyList);
    }

    private Event<Health> getValue(String data) {
        try {
            return new ObjectMapper().readValue(data, new TypeReference<Event<Health>>() {
            });
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage(), ex);

            return null;
        }
    }
}
