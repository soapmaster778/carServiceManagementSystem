package com.example.repairestimates;

import com.example.repaircontract.RepairCatalogGrpc;
import com.example.repaircontract.RepairInfoRequest;
import com.example.repaircontract.RepairInfoResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class RepairCatalogGrpcService extends RepairCatalogGrpc.RepairCatalogImplBase {
    private final RepairCatalog catalog;
    public RepairCatalogGrpcService(RepairCatalog catalog) { this.catalog = catalog; }
    @Override
    public void getRepairInfo(RepairInfoRequest request, StreamObserver<RepairInfoResponse> observer) {
        var info = catalog.find(request.getCode());
        if (info.isEmpty()) {
            observer.onError(Status.NOT_FOUND.withDescription("Repair service not found").asRuntimeException());
            return;
        }
        RepairInfo item = info.get();
        observer.onNext(RepairInfoResponse.newBuilder().setCode(item.code()).setPartsPrice(item.partsPrice())
                .setLaborPrice(item.laborPrice()).setDurationMinutes(item.durationMinutes()).build());
        observer.onCompleted();
    }
}
