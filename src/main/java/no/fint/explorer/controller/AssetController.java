package no.fint.explorer.controller;

import no.fint.explorer.exception.AssetNotFoundException;
import no.fint.explorer.model.Asset;
import no.fint.explorer.service.AssetService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public Flux<Asset> getAssets() {
        return assetService.getAssets();
    }

    @GetMapping("{assetId}")
    public Mono<Asset> getAsset(@PathVariable String assetId) {
        return assetService.getAsset(assetId)
                .switchIfEmpty(Mono.error(AssetNotFoundException::new));
    }

    @GetMapping("{assetId}/components")
    public Flux<Asset.Component> getComponents(@PathVariable String assetId, @RequestParam(required = false) String id) {
        return getAsset(assetId)
                .flatMapIterable(Asset::getComponents)
                .filter(component -> Optional.ofNullable(id)
                        .map(component.getId()::contains)
                        .orElse(true));
    }
}