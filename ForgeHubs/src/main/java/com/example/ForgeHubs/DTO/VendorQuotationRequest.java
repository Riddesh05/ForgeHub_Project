package com.example.ForgeHubs.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorQuotationRequest {
    private LocalDate deliveryDate;
    private String paymentTerms;
    //private String remarks;
    private List<VendorQuotationItemRequest> items = new ArrayList<>();
}
