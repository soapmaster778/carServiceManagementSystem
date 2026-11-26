package com.example.demo.repairinfo;

import com.example.repaircontract.RepairCatalogGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RepairInfoConfiguration {
    @Bean(destroyMethod = "shutdown")
    ManagedChannel repairCatalogChannel(@Value("${repair.catalog.grpc-host:localhost}") String host,
                                        @Value("${repair.catalog.grpc-port:9090}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    RepairInfoLookup repairInfoAdapter(@Value("${repair.catalog.transport:http}") String transport,
                             @Value("${repair.catalog.http-url:http://localhost:8083}") String url,
                             @Value("${repair.catalog.connect-timeout:2s}") Duration connectTimeout,
                             @Value("${repair.catalog.read-timeout:3s}") Duration readTimeout,
                             @Value("${repair.catalog.grpc-deadline:3s}") Duration deadline,
                             ManagedChannel channel) {
        if (connectTimeout.isZero() || connectTimeout.isNegative() || readTimeout.isZero() || readTimeout.isNegative()
                || deadline.toMillis() <= 0) throw new IllegalArgumentException("Repair catalog timeouts must be positive");
        return switch (transport) {
            case "http" -> {
                HttpClient httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
                JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
                factory.setReadTimeout(readTimeout);
                RestClient rest = RestClient.builder().baseUrl(url).requestFactory(factory).build();
                RepairCatalogHttpClient client = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rest))
                        .build().createClient(RepairCatalogHttpClient.class);
                yield new HttpRepairInfoAdapter(client);
            }
            case "grpc" -> new GrpcRepairInfoAdapter(RepairCatalogGrpc.newBlockingStub(channel), deadline);
            default -> throw new IllegalArgumentException("Unknown repair.catalog.transport: " + transport);
        };
    }
}
