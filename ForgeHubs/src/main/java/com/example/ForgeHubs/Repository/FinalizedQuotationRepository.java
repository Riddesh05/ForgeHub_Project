package com.example.ForgeHubs.Repository;

import com.example.ForgeHubs.Entity.FinalizedQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinalizedQuotationRepository extends JpaRepository<FinalizedQuotation, Integer> {

    List<FinalizedQuotation> findByQuotation_Vendor_UserIdOrderByFinalizedDateDesc(Long vendorId);

    Optional<FinalizedQuotation> findByQuotation_QuotationIdAndQuotation_Vendor_UserId(
            Long quotationId,
            Long vendorId
    );

    Optional<FinalizedQuotation> findByRfq_RfqId(Long rfqId);

    Optional<FinalizedQuotation> findByQuotation_QuotationId(Long quotationId);
}
