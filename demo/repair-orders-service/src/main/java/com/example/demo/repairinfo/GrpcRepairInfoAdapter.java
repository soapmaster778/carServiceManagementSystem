package com.example.demo.repairinfo;

import com.example.repaircontract.RepairCatalogGrpc;
import com.example.repaircontract.RepairInfoRequest;
import com.example.repaircontract.RepairInfoResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GrpcRepairInfoAdapter implements RepairInfoLookup {
    private final RepairCatalogGrpc.RepairCatalogBlockingStub stub;
    private final Duration deadline;
    public GrpcRepairInfoAdapter(RepairCatalogGrpc.RepairCatalogBlockingStub stub, Duration deadline) {
        this.stub = stub;
        this.deadline = deadline;
    }
    private RepairInfoResponse find(String code) {
        try {
            return stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                    .getRepairInfo(RepairInfoRequest.newBuilder().setCode(code).build());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) throw new RepairInfoNotFoundException(code);
            throw new RepairInfoUnavailableException(e);
        }
    }
    public BigDecimal getPartsPrice(String code) { return new BigDecimal(find(code).getPartsPrice()); }
    public BigDecimal getLaborPrice(String code) { return new BigDecimal(find(code).getLaborPrice()); }
    public int getDurationMinutes(String code) { return find(code).getDurationMinutes(); }
}
