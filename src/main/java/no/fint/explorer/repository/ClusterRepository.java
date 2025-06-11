package no.fint.explorer.repository;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ClusterRepository {
    private final CoreV1Api coreV1Api;

    private final static String STACK = "fint.stack";

    @Value("${kubernetes.namespace}")
    private String namespace;


    public ClusterRepository(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public Optional<ApiResponse<String>> getApiResponse(V1Service v1Service, String endpoint, String asset) {
        Optional<V1ObjectMeta> metadata = Optional.ofNullable(v1Service.getMetadata());

        String service = metadata
                .map(V1ObjectMeta::getName)
                .orElse(null);

        Integer port = Optional.ofNullable(v1Service.getSpec())
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

        if (service == null || port == null || label == null) {
            return Optional.empty();
        }

        String name = service + ":" + port;

        String path = label.replaceAll("-", "/") + endpoint;

        coreV1Api.getApiClient().addDefaultHeader("x-org-id", asset);

        try {

            return Optional.ofNullable(coreV1Api.connectGetNamespacedServiceProxyWithPathWithHttpInfo(name, namespace, path, null));

        } catch (ApiException ex) {
            log.error("{} - {} - {} - {}", asset, service, endpoint, ex.getMessage());

            return Optional.empty();
        }
    }

    public Optional<V1Service> getNamespacedService(String name) {
        try {
            return Optional.ofNullable(coreV1Api.readNamespacedService(name, namespace, null, null, null));
        } catch (ApiException ex) {
            log.error("Error getting namespaced service {} with error message: {}", name, ex.getMessage());

            return Optional.empty();
        }
    }

    public List<V1Service> getNamespacedServices(String label) {
        try {
            return Optional.ofNullable(coreV1Api.listNamespacedService(namespace, null, null, null, null, label, null, null, null, null))
                    .map(V1ServiceList::getItems)
                    .orElseGet(Collections::emptyList);
        } catch (ApiException ex) {
            log.error("Error getting namespaced service {} with error message: {}", label, ex.getMessage());

            return Collections.emptyList();
        }
    }
}