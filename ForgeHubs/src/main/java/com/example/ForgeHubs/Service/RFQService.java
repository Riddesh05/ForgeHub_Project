package com.example.ForgeHubs.Service;

import com.example.ForgeHubs.DTO.RFQCreateRequest;
import com.example.ForgeHubs.DTO.RFQItemUpdateRequest;
import com.example.ForgeHubs.DTO.RFQResponseDto;
import com.example.ForgeHubs.DTO.UserResponseDto;

import java.util.List;

public interface RFQService {

    String generateRfqNo();

    String generateIndentNo();

    void saveRfq(RFQCreateRequest request, Long adminUserId, boolean draft);

    List<UserResponseDto> getAllVendors();

    List<RFQResponseDto> getAllRFQs();

    RFQResponseDto getRFQById(Long id);

    void updateRFQ(Long id, RFQCreateRequest request, boolean draft);

    void updateRFQItem(RFQItemUpdateRequest request);

    void softDeleteRFQ(Long id);

    void openToRebid(Long id);
}
