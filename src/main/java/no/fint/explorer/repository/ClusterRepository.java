package no.fint.explorer.repository;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ClusterRepository {
    private final CoreV1Api coreV1Api;

    private final static String NAMESPACE = "default";
    private final static String PROVIDER = "fint.role=provider";
    private final static String CONSUMER = "fint.role=consumer";
    private final static String STACK = "fint.stack";

    public ClusterRepository(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public Optional<String> getApiResponse(V1ObjectMeta metadata, String endpoint) {
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

            return Optional.empty();
        }
    }

    public List<V1Pod> getProviders() {
        return getNamespacedPods(PROVIDER);
    }

    public List<V1Pod> getConsumers() {
        return getNamespacedPods(CONSUMER);
    }

    private List<V1Pod> getNamespacedPods(String label) {
        try {
            return Optional.ofNullable(coreV1Api.listNamespacedPod(ClusterRepository.NAMESPACE, null, null, null, null, label, null, null, null, null))
                    .map(V1PodList::getItems)
                    .orElseGet(Collections::emptyList);
        } catch (ApiException ex) {
            log.error("{} - {}", label, ex.getResponseBody());

            return Collections.emptyList();
        }
    }
}