package no.novari.fint.explorer.controller;

import no.novari.fint.explorer.exception.AssetNotFoundException;
import no.novari.fint.explorer.model.Asset;
import no.novari.fint.explorer.service.AssetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// TODO: Map out which services use this endpoint & what its used for
@RestController
@RequestMapping("assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<Asset> getAssets() {
        return assetService.getAssets();
    }

    @GetMapping("{assetId}")
    public Asset getAsset(@PathVariable String assetId) {
        Asset asset = assetService.getAsset(assetId);
        if (asset == null) {
            throw new AssetNotFoundException();
        }
        return asset;
    }

    @GetMapping("{assetId}/components")
    public List<Asset.ComponentStatus> getComponents(@PathVariable String assetId, @RequestParam(required = false) String id) {
        return getAsset(assetId).getComponents().stream()
                .filter(component -> Optional.ofNullable(id)
                        .map(component.getId()::contains)
                        .orElse(true))
                .toList();
    }
}
