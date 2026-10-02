package com.example.ForgeHubs.Repository;

import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.enums.RFQStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RFQRepository extends JpaRepository<RFQ, Integer> {

    Optional<RFQ> findByRfqNo(String rfqNo);

    boolean existsByRfqNo(String rfqNo);

    boolean existsByIndentNo(String indentNo);

    /*
     * RFQ List
     *
     * Includes active + soft deleted RFQs.
     * Soft deleted RFQs will be shown as INACTIVE.
     */
    List<RFQ> findAllByOrderByRfqIdDesc();

    /*
     * Get active RFQ by ID
     */
    Optional<RFQ> findByRfqIdAndIsDeletedFalse(Integer rfqId);

    List<RFQ> findByStatusAndIsDeletedFalseOrderByRfqIdDesc(
            RFQStatus status
    );
}