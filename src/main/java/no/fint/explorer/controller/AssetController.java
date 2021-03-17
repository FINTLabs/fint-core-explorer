package no.fint.explorer.controller;

import no.fint.explorer.exception.AssetNotFoundException;
import no.fint.explorer.exception.ComponentNotFoundException;
import no.fint.explorer.model.Asset;
import no.fint.explorer.service.AssetService;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public Collection<Asset> getAssets() {
        return assetService.getAssets();
    }

    @GetMapping("{assetId}")
    public Asset getAsset(@PathVariable String assetId) {
        return Optional.ofNullable(assetService.getAsset(assetId))
                .orElseThrow(AssetNotFoundException::new);
    }

    @GetMapping("{assetId}/components")
    public List<Asset.Component> getComponents(@PathVariable String assetId, @RequestParam(required = false) String id) {
        return getAsset(assetId).getComponents()
                .stream()
                .filter(component -> Optional.ofNullable(id).map(component.getId()::contains).orElse(true))
                .collect(Collectors.toList());
    }
}