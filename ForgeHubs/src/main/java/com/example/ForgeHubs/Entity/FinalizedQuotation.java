package com.example.ForgeHubs.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "FinalizedQuotations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_Finalized_RFQ",
                        columnNames = "RFQId"
                ),
                @UniqueConstraint(
                        name = "UK_Finalized_Quotation",
                        columnNames = "QuotationId"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinalizedQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FinalId")
    private Integer finalId;

    @Column(name = "FinalizedDate")
    private LocalDateTime finalizedDate;

    // Finalized quotation belongs to RFQ
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "RFQId",
            nullable = false
    )
    private RFQ rfq;

    // Which quotation was finalized
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "QuotationId",
            nullable = false
    )
    private RFQQuotation quotation;
}