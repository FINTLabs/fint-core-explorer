package no.fint.explorer.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.health.Health;
import no.fint.explorer.model.Asset;
import no.fint.explorer.model.SseOrg;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class MetricsService {

    private final static String HEALTHY = "APPLICATION_HEALTHY";
    private final static String UNHEALTHY = "APPLICATION_UNHEALTHY";
    private final static String HEALTH_METRIC = "fint.core.health";
    private final static String CACHE_METRIC = "fint.core.cache";
    private final static String ADAPTER_COUNT_METRIC = "fint.core.adapter.count";
    private final static String ADAPTER_EVENTS_METRIC_TOTAL = "fint.core.adapter.events.total";

    private final MeterRegistry meterRegistry;
    private final AssetService assetService;

    public MetricsService(MeterRegistry meterRegistry, AssetService assetService) {
        this.meterRegistry = meterRegistry;
        this.assetService = assetService;
    }

    @Scheduled(initialDelayString = "${kubernetes.initial-delay}", fixedDelayString = "${kubernetes.fixed-delay}")
    public void update() {

        assetService.update();

        log.info("Start updating metrics...");
        assetService.getAssets().toStream()
                .forEach(asset -> {
                    updateHealthMetric(asset);
                    updateCacheMetric(asset);
                    updateAdapterMetric(asset);
                });
        log.info("Finished updating metrics");
    }

    private void updateHealthMetric(Asset asset) {
        asset.getComponents()
                .forEach(component -> {
                    meterRegistry.gauge(HEALTH_METRIC,
                            Arrays.asList(Tag.of("asset", asset.getId()), Tag.of("component", component.getId())),
                            new AtomicInteger(getHealthStatus(component.getHealth())));
                });
    }

    private void updateCacheMetric(Asset asset) {
        asset.getComponents()
                .forEach(component -> {
                    component
                            .getCache()
                            .forEach(cacheEntry -> {
                                meterRegistry.gauge(CACHE_METRIC,
                                        Arrays.asList(Tag.of("asset", asset.getId()), Tag.of("component", component.getId()), Tag.of("entity", cacheEntry.getName())),
                                        new AtomicInteger(cacheEntry.getSize()));
                            });
                });
    }

    private void updateAdapterMetric(Asset asset) {
        asset.getComponents()
                .forEach(component -> {
                    meterRegistry.gauge(ADAPTER_EVENTS_METRIC_TOTAL,
                            Arrays.asList(Tag.of("asset", asset.getId()), Tag.of("component", component.getId())),
                            new AtomicInteger(component.getClients().stream().mapToInt(SseOrg.SseClient::getEvents).sum()));

                    meterRegistry.gauge(ADAPTER_COUNT_METRIC,
                            Arrays.asList(Tag.of("asset", asset.getId()), Tag.of("component", component.getId())),
                            new AtomicInteger(component.getClients().size()));
                });
    }

    private int getHealthStatus(List<Health> healthList) {
        return healthList
                .stream()
                .map(Health::getStatus)
                .filter(HEALTHY::equals)
                .findFirst()
                .orElse(UNHEALTHY)
                .equals(HEALTHY) ? 1 : 0;
    }
}
