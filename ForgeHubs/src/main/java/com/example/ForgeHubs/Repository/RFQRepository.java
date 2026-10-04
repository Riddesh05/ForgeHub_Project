package com.example.ForgeHubs.Repository;

import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.enums.RFQStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RFQRepository extends JpaRepository<RFQ, Long> {

    Optional<RFQ> findByRfqNo(String rfqNo);

    boolean existsByRfqNo(String rfqNo);

    boolean existsByIndentNo(String indentNo);

    List<RFQ> findAllByOrderByRfqIdDesc();

    Optional<RFQ> findByRfqIdAndIsDeletedFalse(Long rfqId);

    List<RFQ> findByStatusAndIsDeletedFalseOrderByRfqIdDesc(RFQStatus status);
}
