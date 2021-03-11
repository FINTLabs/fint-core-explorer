package no.fint.explorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.extern.slf4j.Slf4j;
import no.fint.explorer.constants.Endpoints;
import no.fint.explorer.model.SseOrg;
import no.fint.explorer.repository.ClusterRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ProviderService {
    private final ClusterRepository clusterRepository;

    private final static String PROVIDER_ROLE = "fint.role=provider";
    private final static String PROVIDER_PREFIX = "provider-";

    public ProviderService(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    public List<V1Service> getProviders() {
        return clusterRepository.getNamespacedServices(PROVIDER_ROLE);
    }

    public Optional<V1Service> getProvider(String id) {
        return clusterRepository.getNamespacedService(PROVIDER_PREFIX + id);
    }

    public List<SseOrg> getSseOrgs(V1Service v1Service) {
        return clusterRepository.getApiResponse(v1Service, Endpoints.SSE_CLIENTS_ENDPOINT, null)
                .map(ApiResponse::getData)
                .map(this::getValue)
                .orElseGet(Collections::emptyList);
    }

    private List<SseOrg> getValue(String data) {
        try {
            return new ObjectMapper().readValue(data, new TypeReference<List<SseOrg>>() {
            });
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage(), ex);

            return null;
        }
    }
}