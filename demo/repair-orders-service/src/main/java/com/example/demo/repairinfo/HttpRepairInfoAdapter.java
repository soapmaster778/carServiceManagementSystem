package com.example.demo.repairinfo;

import java.math.BigDecimal;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

public class HttpRepairInfoAdapter implements RepairInfoLookup {
    private final RepairCatalogHttpClient client;
    public HttpRepairInfoAdapter(RepairCatalogHttpClient client) { this.client = client; }
    private RepairInfo find(String code) {
        try {
            RepairInfo info = client.find(code);
            if (info == null) throw new IllegalStateException("Empty catalog response");
            return info;
        } catch (HttpClientErrorException.NotFound e) {
            throw new RepairInfoNotFoundException(code);
        } catch (RestClientException | IllegalStateException e) {
            throw new RepairInfoUnavailableException(e);
        }
    }
    public BigDecimal getPartsPrice(String code) { return new BigDecimal(find(code).partsPrice()); }
    public BigDecimal getLaborPrice(String code) { return new BigDecimal(find(code).laborPrice()); }
    public int getDurationMinutes(String code) { return find(code).durationMinutes(); }
}
