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

    public Optional<ApiResponse<String>> getApiResponse(V1Service service, String endpoint) {
        Optional<V1ObjectMeta> metadata = Optional.ofNullable(service.getMetadata());

        String pod = metadata
                .map(V1ObjectMeta::getName)
                .orElse(null);

        Integer port = Optional.ofNullable(service.getSpec())
                .map(V1ServiceSpec::getPorts)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(V1ServicePort::getPort)
                .findFirst()
                .orElse(null);

        String label = metadata
                .map(V1ObjectMeta::getLabels)
                .map(labels -> labels.get(STACK))
                .orElse(null);

        if (pod == null || port == null || label == null) {
            return Optional.empty();
        }

        String name = pod + ":" + port;

        String path = label.replaceAll("-", "/") + endpoint;

        try {
            return Optional.ofNullable(coreV1Api.connectGetNamespacedServiceProxyWithPathWithHttpInfo(name, NAMESPACE, path, null));

        } catch (ApiException ex) {
            log.error("{} - {} - {}", service.getMetadata().getName(), endpoint, ex.getMessage());

            return Optional.empty();
        }
    }

    public List<V1Service> getProviders() {
        return getNamespacedServices(PROVIDER);
    }

    public List<V1Service> getConsumers() {
        return getNamespacedServices(CONSUMER);
    }

    private List<V1Service> getNamespacedServices(String label) {
        try {
            return Optional.ofNullable(coreV1Api.listNamespacedService(ClusterRepository.NAMESPACE, null, null, null, null, label, null, null, null, null))
                    .map(V1ServiceList::getItems)
                    .orElseGet(Collections::emptyList);
        } catch (ApiException ex) {
            log.error("{} - {}", label, ex.getResponseBody());

            return Collections.emptyList();
        }
    }
}