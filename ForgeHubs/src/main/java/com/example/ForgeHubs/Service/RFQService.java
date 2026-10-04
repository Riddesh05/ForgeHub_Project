package com.example.ForgeHubs.Service;

import com.example.ForgeHubs.DTO.RFQCreateRequest;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQItem;
import com.example.ForgeHubs.Entity.User;

import java.time.LocalDate;
import java.util.List;

public interface RFQService {

    String generateRfqNo();

    String generateIndentNo();

    void saveRfq(RFQCreateRequest request, Long adminUserId, boolean draft);

    List<User> getAllVendors();

    List<RFQ> getAllRFQs();

    RFQ getRFQById(Integer id);

    List<RFQItem> getRFQItems(Integer rfqId);

    void updateRFQ(Integer id, RFQCreateRequest request, boolean draft);

    void updateRFQItem(
            Integer itemId,
            String itemName,
            Integer reqQty,
            String uom,
            LocalDate reqDeliveryDate,
            String deliveryLocation,
            String description
    );

    void softDeleteRFQ(Integer id);

    void openToRebid(Integer id);
}
