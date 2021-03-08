package no.fint.explorer.controller;

import no.fint.explorer.exception.AssetNotFoundException;
import no.fint.explorer.exception.ComponentNotFoundException;
import no.fint.explorer.model.Asset;
import no.fint.explorer.service.AssetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("{id}")
    public Asset getAsset(@PathVariable String id) {
        return Optional.ofNullable(assetService.getAsset(id))
                .orElseThrow(AssetNotFoundException::new);
    }

    @GetMapping("{id}/components")
    public List<Asset.Component> getComponents(@PathVariable String id) {
        return getAsset(id).getComponents();
    }

    @GetMapping("{assetId}/components/{componentId}")
    public Asset.Component getComponent(@PathVariable String assetId, @PathVariable String componentId) {
        return getComponents(assetId)
                .stream()
                .filter(component -> component.getId().equals(componentId))
                .findFirst()
                .orElseThrow(ComponentNotFoundException::new);
    }
}