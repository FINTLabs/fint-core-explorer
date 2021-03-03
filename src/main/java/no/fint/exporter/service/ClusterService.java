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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Service
public class ClusterService {
    private final CoreV1Api inClusterClient;

    private final static String NAMESPACE = "default";
    private final static String PROVIDER = "fint.role=provider";
    private final static String CONSUMER = "fint.role=consumer";
    private final static String STACK = "fint.stack";

    public ClusterService(CoreV1Api inClusterClient) {
        this.inClusterClient = inClusterClient;
    }

    public Stream<SseOrg> getAdapters() {
        return getProviders()
                .stream()
                .map(V1Pod::getMetadata)
                .map(getSseOrgs)
                .flatMap(List::stream);
    }

    private final Function<V1ObjectMeta, List<SseOrg>> getSseOrgs = metadata -> {
        String response = getApiResponse(metadata, "/provider/sse/clients");

        if (response == null || response.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return new ObjectMapper().readValue(response, new TypeReference<List<SseOrg>>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    };

    private String getApiResponse(V1ObjectMeta metadata, String endpoint) {
        try {
            String pod = metadata.getName();

            String label = Optional.ofNullable(metadata.getLabels())
                    .map(labels -> labels.get(STACK))
                    .orElse(null);

            if (pod == null || label == null) {
                return null;
            }

            String path = label.replaceAll("-", "/") + endpoint;

            ApiResponse<String> response = inClusterClient.connectGetNamespacedPodProxyWithPathWithHttpInfo(pod, NAMESPACE, path, null);

            return response.getData();

        } catch (ApiException e) {
            log.error(metadata.getName(), e.getMessage());
        }

        return null;
    }

    private List<V1Pod> getProviders() {
        return getNamespacedPods(PROVIDER);
    }

    private List<V1Pod> getConsumers() {
        return getNamespacedPods(CONSUMER);
    }

    private List<V1Pod> getNamespacedPods(String label) {
        try {
            return inClusterClient.listNamespacedPod(ClusterService.NAMESPACE, null, null, null, null, label, null, null, null, null).getItems();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}