package com.example.demo.repairinfo;

import com.example.repaircontract.RepairCatalogGrpc;
import com.example.repaircontract.RepairInfoRequest;
import com.example.repaircontract.RepairInfoResponse;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import java.math.BigDecimal;
import java.time.Duration;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepairInfoAdapterTest {
    @Test
    void declarativeHttpClientCallsServerAndMapsHttpErrors() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/repair-catalog", exchange -> {
            String path = exchange.getRequestURI().getPath();
            int status = path.endsWith("brakes") ? 200 : path.endsWith("missing") ? 404 : 503;
            byte[] body = (status == 200 ? "{\"code\":\"brakes\",\"partsPrice\":\"1500.00\",\"laborPrice\":\"800.00\",\"durationMinutes\":90}" : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (var stream = exchange.getResponseBody()) { stream.write(body); }
        });
        server.start();
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        try {
            RepairInfoLookup adapter = new RepairInfoConfiguration().repairInfoAdapter("http",
                    "http://localhost:" + server.getAddress().getPort(), Duration.ofSeconds(1),
                    Duration.ofSeconds(2), Duration.ofSeconds(1), channel);
            assertThat(adapter.getPartsPrice("brakes")).isEqualByComparingTo("1500");
            assertThat(adapter.getLaborPrice("brakes")).isEqualByComparingTo("800");
            assertThat(adapter.getDurationMinutes("brakes")).isEqualTo(90);
            assertThatThrownBy(() -> adapter.getPartsPrice("missing")).isInstanceOf(RepairInfoNotFoundException.class);
            assertThatThrownBy(() -> adapter.getPartsPrice("error")).isInstanceOf(RepairInfoUnavailableException.class);
        } finally { server.stop(0); channel.shutdownNow(); }
    }

    @Test
    void httpAdapterReturnsAllFieldsAndMapsFailures() {
        RepairCatalogHttpClient client = mock(RepairCatalogHttpClient.class);
        HttpRepairInfoAdapter adapter = new HttpRepairInfoAdapter(client);
        when(client.find("brakes")).thenReturn(new RepairInfo("brakes", "1500.00", "800.00", 90));
        assertThat(adapter.getPartsPrice("brakes")).isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(adapter.getLaborPrice("brakes")).isEqualByComparingTo(new BigDecimal("800"));
        assertThat(adapter.getDurationMinutes("brakes")).isEqualTo(90);
        when(client.find("missing")).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "", null, null, null));
        assertThatThrownBy(() -> adapter.getPartsPrice("missing")).isInstanceOf(RepairInfoNotFoundException.class);
        when(client.find("offline")).thenThrow(new ResourceAccessException("Connection refused"));
        assertThatThrownBy(() -> adapter.getPartsPrice("offline")).isInstanceOf(RepairInfoUnavailableException.class);
    }

    @Test
    void grpcAdapterUsesServerAndMapsNotFoundAndUnavailable() throws Exception {
        Server server = ServerBuilder.forPort(0).addService(new RepairCatalogGrpc.RepairCatalogImplBase() {
            @Override public void getRepairInfo(RepairInfoRequest request, StreamObserver<RepairInfoResponse> observer) {
                if (!request.getCode().equals("brakes")) { observer.onError(Status.NOT_FOUND.asRuntimeException()); return; }
                observer.onNext(RepairInfoResponse.newBuilder().setCode("brakes").setPartsPrice("1500.00")
                        .setLaborPrice("800.00").setDurationMinutes(90).build());
                observer.onCompleted();
            }
        }).build().start();
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", server.getPort()).usePlaintext().build();
        try {
            GrpcRepairInfoAdapter adapter = new GrpcRepairInfoAdapter(RepairCatalogGrpc.newBlockingStub(channel), Duration.ofSeconds(2));
            assertThat(adapter.getPartsPrice("brakes")).isEqualByComparingTo("1500");
            assertThat(adapter.getLaborPrice("brakes")).isEqualByComparingTo("800");
            assertThat(adapter.getDurationMinutes("brakes")).isEqualTo(90);
            assertThatThrownBy(() -> adapter.getPartsPrice("missing")).isInstanceOf(RepairInfoNotFoundException.class);
            server.shutdownNow();
            assertThatThrownBy(() -> adapter.getPartsPrice("brakes")).isInstanceOf(RepairInfoUnavailableException.class);
        } finally { channel.shutdownNow(); server.shutdownNow(); }
    }

    @Test
    void grpcDeadlineStopsWaitingForSlowService() throws Exception {
        Server server = ServerBuilder.forPort(0).addService(new RepairCatalogGrpc.RepairCatalogImplBase() {
            @Override public void getRepairInfo(RepairInfoRequest request, StreamObserver<RepairInfoResponse> observer) {
                // Deliberately leave the call unanswered to exercise the client deadline.
            }
        }).build().start();
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", server.getPort()).usePlaintext().build();
        try {
            GrpcRepairInfoAdapter adapter = new GrpcRepairInfoAdapter(RepairCatalogGrpc.newBlockingStub(channel), Duration.ofMillis(100));
            assertThatThrownBy(() -> adapter.getPartsPrice("brakes"))
                    .isInstanceOf(RepairInfoUnavailableException.class)
                    .hasCauseInstanceOf(io.grpc.StatusRuntimeException.class);
        } finally { channel.shutdownNow(); server.shutdownNow(); }
    }

    @Test
    void unknownTransportFailsAtStartup() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        try {
            assertThatThrownBy(() -> new RepairInfoConfiguration().repairInfoAdapter("invalid", "http://localhost:8083",
                    Duration.ofSeconds(1), Duration.ofSeconds(1), Duration.ofSeconds(1), channel))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("invalid");
        } finally { channel.shutdownNow(); }
    }
}
