package com.example.ForgeHubs.Service;

import com.example.ForgeHubs.DTO.VendorQuotationHistory;
import com.example.ForgeHubs.DTO.VendorQuotationRequest;
import com.example.ForgeHubs.Entity.FinalizedQuotation;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQQuotation;
import com.example.ForgeHubs.Entity.User;

import java.util.List;

public interface VendorService {

    User getVendor(Integer vendorId);

    List<RFQ> getOpenRfqs(Integer vendorId);

    RFQ getAssignedRfq(Integer rfqId, Integer vendorId);

    void submitQuotation(Integer rfqId, Integer vendorId, VendorQuotationRequest request);

    List<RFQQuotation> getMySubmissions(Integer vendorId);

    RFQQuotation getMySubmission(Integer quotationId, Integer vendorId);

    List<FinalizedQuotation> getFinalizedQuotations(Integer vendorId);

    // Admin-side quotation flow
    List<RFQQuotation> getAllVendorQuotations();

    RFQQuotation getQuotationForAdmin(Integer quotationId);

    void finalizeQuotation(Integer quotationId);


//    List<VendorQuotationHistory> getQuotationHistory(
//            Integer quotationId,
//            Integer vendorId
//    );
}
