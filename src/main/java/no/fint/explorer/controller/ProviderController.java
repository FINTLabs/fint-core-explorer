package no.fint.explorer.controller;

import no.fint.explorer.model.Asset;
import no.fint.explorer.service.ProviderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("provider")
public class ProviderController {
    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("assets")
    public List<Asset> getAssets() {
        return providerService.getAssets();
    }
}