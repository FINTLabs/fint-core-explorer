package no.fint.explorer.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.explorer.repository.ClusterRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsumerService {
    private final ClusterRepository clusterRepository;

    public ConsumerService(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }
}
