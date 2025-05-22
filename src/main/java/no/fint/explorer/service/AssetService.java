package no.fint.explorer.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.health.Health;
import no.fint.explorer.factory.AssetFactory;
import no.fint.explorer.model.Asset;
import no.fint.explorer.model.CacheEntry;
import no.fint.explorer.model.SseOrg;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssetService {
    private final ProviderService providerService;
    private final ConsumerService consumerService;

    private final Map<String, Asset> assets = new ConcurrentSkipListMap<>();

    public AssetService(ProviderService providerService, ConsumerService consumerService) {
        this.providerService = providerService;
        this.consumerService = consumerService;
    }

    public Flux<Asset> getAssets() {
        return Flux.fromIterable(assets.values());
    }

    public Mono<Asset> getAsset(String id) {
        return Mono.justOrEmpty(assets.get(id));
    }

    @CacheEvict(value = {"consumers", "caches"}, allEntries = true)
    public void update() {
        log.info("Start collect data...");

        providerService.getProviders()
                .parallelStream()
                .map(providerService::getSseOrgs)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(SseOrg::getOrgId))
                .entrySet()
                .parallelStream()
                .map(AssetFactory::toAsset)
                .peek(updateHealthAndCache())
                .forEach(asset -> assets.put(asset.getId(), asset));

        log.info("Finished collecting data");
    }

    private Consumer<Asset> updateHealthAndCache() {
        return asset ->
            CompletableFuture.allOf(asset.getComponents().stream()
                    .map(component -> CompletableFuture.runAsync(() -> {
                        component.setLastUpdated(ZonedDateTime.now(ZoneId.of("Z")));

                        if (component.getClients().isEmpty()) {
                            return;
                        }

                        consumerService.getConsumer(component.getId()).ifPresent(service -> {
                            List<Health> health = consumerService.getHealth(service, asset.getId());
                            component.setHealth(health);

                            List<CacheEntry> cache = consumerService.getCache(service)
                                    .getOrDefault(asset.getId(), Collections.emptyMap())
                                    .entrySet()
                                    .parallelStream()
                                    .map(this::updateCacheEntry)
                                    .collect(Collectors.toList());

                            component.setCache(cache);
                        });
                    })).toArray(CompletableFuture[]::new)).join();
    }

    private CacheEntry updateCacheEntry(Map.Entry<String, CacheEntry> cacheEntry) {
        CacheEntry entry = cacheEntry.getValue();
        entry.setName(cacheEntry.getKey());

        return entry;
    }
}