package no.fint.exporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;
import no.fint.exporter.model.SseOrg;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Service
public class ClusterService {
    private final CoreV1Api coreV1Api;

    private final static String NAMESPACE = "default";
    private final static String PROVIDER = "fint.role=provider";
    private final static String CONSUMER = "fint.role=consumer";
    private final static String STACK = "fint.stack";

    public ClusterService(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public Stream<SseOrg> getAdapters() {
        return getProviders()
                .stream()
                .map(V1Pod::getMetadata)
                .map(getSseOrgs)
                .flatMap(List::stream);
    }

    private final Function<V1ObjectMeta, List<SseOrg>> getSseOrgs = metadata -> {
        List<SseOrg> sseOrgs = new ArrayList<>();

        getApiResponse(metadata, "/provider/sse/clients").ifPresent(data -> {
            try {
                sseOrgs.addAll(new ObjectMapper().readValue(data, new TypeReference<List<SseOrg>>() {
                }));
            } catch (JsonProcessingException ex) {
                log.error(ex.getMessage(), ex);
            }
        });

        return sseOrgs;
    };

    private Optional<String> getApiResponse(V1ObjectMeta metadata, String endpoint) {
        String pod = metadata.getName();

        String label = Optional.ofNullable(metadata.getLabels())
                .map(labels -> labels.get(STACK))
                .orElse(null);

        if (pod == null || label == null) {
            return Optional.empty();
        }

        String path = label.replaceAll("-", "/") + endpoint;

        try {
            return Optional.ofNullable(coreV1Api.connectGetNamespacedPodProxyWithPathWithHttpInfo(pod, NAMESPACE, path, null))
                    .map(ApiResponse::getData);

        } catch (ApiException ex) {
            log.error("{} - {} - {}", metadata.getName(), endpoint, ex.getMessage());
        }

        return Optional.empty();
    }

    private List<V1Pod> getProviders() {
        return getNamespacedPods(PROVIDER);
    }

    private List<V1Pod> getConsumers() {
        return getNamespacedPods(CONSUMER);
    }

    private List<V1Pod> getNamespacedPods(String label) {
        try {
            return Optional.ofNullable(coreV1Api.listNamespacedPod(ClusterService.NAMESPACE, null, null, null, null, label, null, null, null, null))
                    .map(V1PodList::getItems)
                    .orElseGet(Collections::emptyList);
        } catch (ApiException ex) {
            log.error("{} - {}", label, ex.getResponseBody());
        }

        return Collections.emptyList();
    }
}