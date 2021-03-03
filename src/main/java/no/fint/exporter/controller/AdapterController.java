package no.fint.exporter.controller;

import no.fint.exporter.model.SseOrg;
import no.fint.exporter.service.ClusterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("adapters")
public class AdapterController {
    private final ClusterService clusterService;

    public AdapterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping
    public Stream<SseOrg> getAdapters() {
        return clusterService.getAdapters();
    }
}