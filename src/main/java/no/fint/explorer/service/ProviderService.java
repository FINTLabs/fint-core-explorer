package no.fint.explorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.extern.slf4j.Slf4j;
import no.fint.explorer.model.SseOrg;
import no.fint.explorer.repository.ClusterRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class ProviderService {
    private final ClusterRepository clusterRepository;

    public ProviderService(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    public Stream<SseOrg> getAdapters() {
        return clusterRepository.getProviders()
                .stream()
                .map(this::getSseOrgs)
                .flatMap(List::stream);
    }

    private List<SseOrg> getSseOrgs(V1Service service) {
        List<SseOrg> sseOrgs = new ArrayList<>();

        clusterRepository.getApiResponse(service, "/provider/sse/clients")
                .map(ApiResponse::getData)
                .ifPresent(data -> {
                    try {
                        sseOrgs.addAll(new ObjectMapper().readValue(data, new TypeReference<List<SseOrg>>() {
                        }));
                    } catch (JsonProcessingException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });

        return sseOrgs;
    }
}
