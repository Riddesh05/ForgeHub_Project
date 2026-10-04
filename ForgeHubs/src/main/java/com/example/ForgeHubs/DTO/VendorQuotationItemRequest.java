package com.example.ForgeHubs.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorQuotationItemRequest {
    private Long itemId;
    private Integer availableQty;
    private BigDecimal unitPrice;
   // private BigDecimal otherCharges;
}
