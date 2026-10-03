package com.example.ForgeHubs.Service;

import com.example.ForgeHubs.DTO.VendorQuotationHistory;
import com.example.ForgeHubs.DTO.VendorQuotationRequest;
import com.example.ForgeHubs.Entity.FinalizedQuotation;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQQuotation;
import com.example.ForgeHubs.Entity.User;

import java.util.List;

public interface VendorService {

    User getVendor(Long vendorId);

    List<RFQ> getOpenRfqs(Long vendorId);

    RFQ getAssignedRfq(Long rfqId, Long vendorId);

    void submitQuotation(Long rfqId, Long vendorId, VendorQuotationRequest request);

    List<RFQQuotation> getMySubmissions(Long vendorId);

    RFQQuotation getMySubmission(Long quotationId, Long vendorId);

    List<FinalizedQuotation> getFinalizedQuotations(Long vendorId);

    // Admin-side quotation flow
    List<RFQQuotation> getAllVendorQuotations();

    RFQQuotation getQuotationForAdmin(Long quotationId);

    void finalizeQuotation(Long quotationId);


//    List<VendorQuotationHistory> getQuotationHistory(
//            Integer quotationId,
//            Integer vendorId
//    );
}
