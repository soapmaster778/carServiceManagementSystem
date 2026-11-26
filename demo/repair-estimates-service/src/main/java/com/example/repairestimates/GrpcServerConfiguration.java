package com.example.repairestimates;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "repair.grpc.enabled", havingValue = "true")
public class GrpcServerConfiguration {
    @Bean(destroyMethod = "shutdown")
    Server repairGrpcServer(RepairCatalogGrpcService service, @Value("${repair.grpc.port:9090}") int port) throws IOException {
        return ServerBuilder.forPort(port).addService(service).build().start();
    }
}
