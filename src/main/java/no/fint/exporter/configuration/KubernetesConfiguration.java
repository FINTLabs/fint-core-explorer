package no.fint.exporter.configuration;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class KubernetesConfiguration {

    @Bean
    public CoreV1Api inClusterClient() throws IOException {
        ApiClient client = ClientBuilder.cluster().build();

        CoreV1Api coreV1Api = new CoreV1Api();
        coreV1Api.setApiClient(client);

        return coreV1Api;
    }
}
