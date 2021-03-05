package no.fint.explorer.controller;

import no.fint.explorer.exception.AssetNotFoundException;
import no.fint.explorer.model.Asset;
import no.fint.explorer.service.ProviderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("provider")
public class ProviderController {
    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("assets")
    public Set<Asset> getAssets() {
        return providerService.getAssets();
    }

    @GetMapping("assets/{id}")
    public Asset getAssets(@PathVariable String id) {
        return providerService.getAsset(id)
                .orElseThrow(AssetNotFoundException::new);
    }
}