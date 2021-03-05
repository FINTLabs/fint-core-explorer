package no.fint.explorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.extern.slf4j.Slf4j;
import no.fint.explorer.model.Asset;
import no.fint.explorer.model.SseOrg;
import no.fint.explorer.repository.ClusterRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProviderService {
    private final ClusterRepository clusterRepository;

    private final Set<Asset> assets = new ConcurrentSkipListSet<>(Comparator.comparing(Asset::getId));

    public ProviderService(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 300000)
    public void run() {
        clusterRepository.getProviders()
                .stream()
                .map(this::getSseOrgs)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(SseOrg::getOrgId))
                .entrySet()
                .stream()
                .map(this::toAsset)
                .peek(assets::remove)
                .forEach(assets::add);
    }

    public Set<Asset> getAssets() {
        return assets;
    }

    public Optional<Asset> getAsset(String id) {
        return assets.stream()
                .filter(asset -> asset.getId().equals(id))
                .findFirst();
    }

    private List<SseOrg> getSseOrgs(V1Service v1Service) {
        List<SseOrg> sseOrgs = new ArrayList<>();

        clusterRepository.getApiResponse(v1Service, "/provider/sse/clients")
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

    private Asset toAsset(Map.Entry<String, List<SseOrg>> entry) {
        Asset asset = new Asset();

        asset.setId(entry.getKey());
        asset.setLastUpdated(ZonedDateTime.now());

        entry.getValue()
                .stream()
                .map(this::toComponent)
                .forEach(asset.getComponents()::add);

        return asset;
    }

    private Asset.Component toComponent(SseOrg sseOrg) {
        Asset.Component component = new Asset.Component();

        component.setPath(sseOrg.getPath());
        component.setClients(sseOrg.getClients());

        return component;
    }
}