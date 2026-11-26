package com.example.repairestimates;

import com.example.repaircontract.RepairInfoRequest;
import com.example.repaircontract.RepairInfoResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RepairCatalogGrpcServiceTest {
    @Test
    @SuppressWarnings("unchecked")
    void missingServiceReturnsNotFound() {
        StreamObserver<RepairInfoResponse> observer = mock(StreamObserver.class);
        new RepairCatalogGrpcService(new RepairCatalog()).getRepairInfo(
                RepairInfoRequest.newBuilder().setCode("missing").build(), observer);
        var failure = org.mockito.ArgumentCaptor.forClass(Throwable.class);
        verify(observer).onError(failure.capture());
        assertThat(Status.fromThrowable(failure.getValue()).getCode()).isEqualTo(Status.Code.NOT_FOUND);
        verify(observer, never()).onNext(any());
    }
}
