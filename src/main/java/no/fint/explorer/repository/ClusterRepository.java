package no.fint.explorer.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.health.Health;
import no.fint.explorer.Endpoints;
import no.fint.explorer.service.ConsumerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Repository
public class ClusterRepository {
    private final CoreV1Api coreV1Api;
    private final MeterRegistry meterRegistry;

    private final static String STACK = "fint.stack";
    private final static String HEALTHY = "APPLICATION_HEALTHY";
    private final static String UNHEALTHY = "APPLICATION_UNHEALTHY";
    private final static String HEALTH_METRIC = "fint.core.health";

    @Value("${kubernetes.namespace}")
    private String namespace;

    private final Map<String, AtomicInteger> gauges = new ConcurrentHashMap<>();

    public ClusterRepository(CoreV1Api coreV1Api, MeterRegistry meterRegistry) {
        this.coreV1Api = coreV1Api;
        this.meterRegistry = meterRegistry;
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
            Optional<ApiResponse<String>> response = Optional.ofNullable(coreV1Api.connectGetNamespacedServiceProxyWithPathWithHttpInfo(name, namespace, path, null));

            updateMetrics(asset, service, endpoint, response);

            return response;

        } catch (ApiException ex) {
            log.error("{} - {} - {} - {}", asset, service, endpoint, ex.getMessage());

            updateMetrics(asset, service, endpoint, Optional.empty());

            return Optional.empty();
        }
    }

    public Optional<V1Service> getNamespacedService(String name) {
        try {
            return Optional.ofNullable(coreV1Api.readNamespacedService(name, namespace, null, null, null));
        } catch (ApiException ex) {
            log.error("{} - {}", name, ex.getResponseBody());

            return Optional.empty();
        }
    }

    public List<V1Service> getNamespacedServices(String label) {
        try {
            return Optional.ofNullable(coreV1Api.listNamespacedService(namespace, null, null, null, null, label, null, null, null, null))
                    .map(V1ServiceList::getItems)
                    .orElseGet(Collections::emptyList);
        } catch (ApiException ex) {
            log.error("{} - {}", label, ex.getResponseBody());

            return Collections.emptyList();
        }
    }

    private void updateMetrics(String asset, String service, String endpoint, Optional<ApiResponse<String>> response) {
        if (!service.startsWith(ConsumerService.CONSUMER_PREFIX)) {
            return;
        }

        String component = StringUtils.substringAfter(service, "-");

        if (endpoint.equals(Endpoints.ADMIN_HEALTH_ENDPOINT)) {
            String gauge = asset.concat(component);

            int status = getStatus(response).equals(HEALTHY) ? 1 : 0;

            gauges.computeIfPresent(gauge, (key, value) -> {
                value.set(status);
                return value;
            });

            gauges.putIfAbsent(gauge, meterRegistry.gauge(HEALTH_METRIC,
                    Arrays.asList(Tag.of("asset", asset), Tag.of("component", component)),
                    new AtomicInteger(status)));
        }
    }

    private String getStatus(Optional<ApiResponse<String>> response) {
        return response
                .map(ApiResponse::getData)
                .map(this::getValue)
                .map(Event::getData)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Health::getStatus)
                .filter(HEALTHY::equals)
                .findFirst()
                .orElse(UNHEALTHY);
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

    private String getException(ApiException exception) {
        return Optional.ofNullable(exception)
                .map(ApiException::getCause)
                .map(Throwable::getClass)
                .map(Class::getSimpleName)
                .orElse("NONE");
    }
}