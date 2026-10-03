package com.example.ForgeHubs.Repository;

import com.example.ForgeHubs.Entity.FinalizedQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinalizedQuotationRepository extends JpaRepository<FinalizedQuotation, Integer> {

    List<FinalizedQuotation> findByQuotation_Vendor_UserIdOrderByFinalizedDateDesc(Integer vendorId);

    Optional<FinalizedQuotation> findByQuotation_QuotationIdAndQuotation_Vendor_UserId(
            Integer quotationId,
            Integer vendorId
    );

    Optional<FinalizedQuotation> findByRfq_RfqId(Integer rfqId);

    Optional<FinalizedQuotation> findByQuotation_QuotationId(Integer quotationId);
}
