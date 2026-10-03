package com.example.ForgeHubs.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "RFQQuotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RFQQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuotationId")
    private Long quotationId;

    @Column(
            name = "BidNo",
            columnDefinition = "LONGTEXT"
    )
    private String bidNo;

    @Column(
            name = "QuotedAmount",
            precision = 18,
            scale = 4
    )
    private BigDecimal quotedAmount;

    @Column(name = "DeliveryDate")
    private LocalDate deliveryDate;

    @Column(
            name = "PaymentTerms",
            columnDefinition = "LONGTEXT"
    )
    private String paymentTerms;

    @Column(
            name = "Remarks",
            columnDefinition = "LONGTEXT"
    )
    private String remarks;

    @Column(
            name = "Status",
            columnDefinition = "LONGTEXT"
    )
    private String status;

    @Column(name = "SubmittedDate")
    private LocalDateTime submittedDate;

    // Quotation belongs to RFQ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "RFQId",
            nullable = false
    )
    private RFQ rfq;

    // Quotation submitted by Vendor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "VendorId",
            nullable = false
    )
    private User vendor;
}