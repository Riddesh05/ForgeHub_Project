package com.example.ForgeHubs.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorQuotationItemResponseDto {
    private Long itemId;
    private String itemName;
    private Integer requiredQty;
    private Integer availableQty;
    private String uom;
    private BigDecimal unitPrice;
   // private BigDecimal otherCharges;
    private BigDecimal itemSubtotal;
    private BigDecimal subtotal;
}
