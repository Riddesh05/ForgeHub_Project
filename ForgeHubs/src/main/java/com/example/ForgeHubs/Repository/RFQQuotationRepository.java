package com.example.ForgeHubs.Repository;

import com.example.ForgeHubs.Entity.RFQQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RFQQuotationRepository extends JpaRepository<RFQQuotation, Long> {

    List<RFQQuotation> findAllByOrderBySubmittedDateDesc();

    List<RFQQuotation> findByVendor_UserIdOrderBySubmittedDateDesc(Long vendorId);

    Optional<RFQQuotation> findByRfq_RfqIdAndVendor_UserId(Long rfqId, Long vendorId);

    Optional<RFQQuotation> findByQuotationIdAndVendor_UserId(Long quotationId, Long vendorId);
}
