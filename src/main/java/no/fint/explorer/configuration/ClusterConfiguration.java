package no.fint.explorer.configuration;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;

@Configuration
public class ClusterConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "kubernetes", name = "client", havingValue = "in-cluster")
    public CoreV1Api inClusterClient() throws IOException {
        ApiClient client = ClientBuilder.cluster().build();

        CoreV1Api coreV1Api = new CoreV1Api();
        coreV1Api.setApiClient(client);

        return coreV1Api;
    }

    @Bean
    @ConditionalOnProperty(prefix = "kubernetes", name = "client", havingValue = "kube-config-file")
    public CoreV1Api kubeConfigFileClient() throws IOException {
        String kubeConfigPath = System.getenv("HOME") + "/.kube/config";

        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        CoreV1Api coreV1Api = new CoreV1Api();
        coreV1Api.setApiClient(client);

        return coreV1Api;
    }
}
