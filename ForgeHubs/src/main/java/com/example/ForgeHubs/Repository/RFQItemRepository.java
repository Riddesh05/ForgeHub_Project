package com.example.ForgeHubs.Repository;

import com.example.ForgeHubs.Entity.RFQItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RFQItemRepository extends JpaRepository<RFQItem, Long> {

    List<RFQItem> findByRfq_RfqId(Long rfqId);
}
