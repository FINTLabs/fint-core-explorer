package no.fint.exporter.service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class KubernetesService {
    private final CoreV1Api inClusterClient;

    public KubernetesService(CoreV1Api inClusterClient) {
        this.inClusterClient = inClusterClient;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void test() throws ApiException {
        V1PodList list = inClusterClient.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

        for (V1Pod item : list.getItems()) {
            log.info(Optional.ofNullable(item.getMetadata()).map(V1ObjectMeta::getName).orElse(null));
        }
    }
}
