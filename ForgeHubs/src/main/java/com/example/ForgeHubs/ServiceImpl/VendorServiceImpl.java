        package com.example.ForgeHubs.ServiceImpl;

import com.example.ForgeHubs.DTO.VendorQuotationItemRequest;
import com.example.ForgeHubs.DTO.VendorQuotationRequest;
import com.example.ForgeHubs.Entity.FinalizedQuotation;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQItem;
import com.example.ForgeHubs.Entity.RFQQuotation;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Repository.FinalizedQuotationRepository;
import com.example.ForgeHubs.Repository.RFQQuotationRepository;
import com.example.ForgeHubs.Repository.RFQVendorRepository;
import com.example.ForgeHubs.Repository.UserRepository;
import com.example.ForgeHubs.Service.VendorService;
import com.example.ForgeHubs.enums.RFQStatus;
import com.example.ForgeHubs.enums.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private static final BigDecimal GST_RATE = new BigDecimal("0.10");
    private static final String QUOTATION_JSON_PREFIX = "FORGEHUB_QUOTATION_V1:";

    private final UserRepository userRepository;
    private final RFQVendorRepository rfqVendorRepository;
    private final RFQQuotationRepository rfqQuotationRepository;
    private final FinalizedQuotationRepository finalizedQuotationRepository;
    private final ObjectMapper objectMapper;

    @Override
    public User getVendor(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + vendorId));

        if (vendor.getRole() != UserRole.VENDOR) {
            throw new RuntimeException("Selected user is not a vendor: " + vendor.getName());
        }

        return vendor;
    }

    @Override
    @Transactional
    public List<RFQ> getOpenRfqs(Long vendorId) {
        getVendor(vendorId);

        List<RFQ> rfqs = rfqVendorRepository.findActiveAssignedRfqs(vendorId);
        List<RFQ> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (RFQ rfq : rfqs) {
            if (rfq.getExpiryDateOfBid() != null
                    && today.isAfter(rfq.getExpiryDateOfBid().toLocalDate())) {
                continue;
            }

            if (rfq.getItems() != null) {
                rfq.getItems().size();
            }

            result.add(rfq);
        }

        return result;
    }

    @Override
    @Transactional
    public RFQ getAssignedRfq(Long rfqId, Long vendorId) {
        getVendor(vendorId);

        if (!rfqVendorRepository.existsByRfq_RfqIdAndVendor_UserId(rfqId, vendorId)) {
            throw new RuntimeException("This RFQ is not assigned to the selected vendor");
        }

        RFQ rfq = rfqVendorRepository
                .findByRfq_RfqIdAndVendor_UserId(rfqId, vendorId)
                .orElseThrow(() -> new RuntimeException("RFQ assignment not found"))
                .getRfq();

        if (Boolean.TRUE.equals(rfq.getIsDeleted())) {
            throw new RuntimeException("This RFQ is inactive");
        }

        if (rfq.getItems() != null) {
            rfq.getItems().size();
        }

        return rfq;
    }

    @Override
    @Transactional
    public void submitQuotation(
            Long rfqId,
            Long vendorId,
            VendorQuotationRequest request
    ) {
        User vendor = getVendor(vendorId);
        RFQ rfq = getAssignedRfq(rfqId, vendorId);

        if (rfq.getStatus() != RFQStatus.OPEN && rfq.getStatus() != RFQStatus.REOPENED) {
            throw new RuntimeException("Quotation can only be submitted for an OPEN or REOPENED RFQ");
        }

        if (rfq.getExpiryDateOfBid() != null
                && LocalDate.now().isAfter(rfq.getExpiryDateOfBid().toLocalDate())) {
            throw new RuntimeException("Bid submission date has expired");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Quotation must contain at least one item");
        }

        Map<Long, RFQItem> rfqItems = new LinkedHashMap<>();
        for (RFQItem item : rfq.getItems()) {
            rfqItems.put(item.getItemId(), item);
        }

        List<Map<String, Object>> quotationItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (VendorQuotationItemRequest itemRequest : request.getItems()) {
            RFQItem item = rfqItems.get(itemRequest.getItemId());

            if (item == null) {
                throw new RuntimeException("Invalid RFQ item selected: " + itemRequest.getItemId());
            }

            int availableQty = itemRequest.getAvailableQty() == null
                    ? 0
                    : itemRequest.getAvailableQty();

            if (availableQty < 0
                    || (item.getReqQty() != null && availableQty > item.getReqQty())) {
                throw new RuntimeException(
                        "Available quantity is invalid for item: " + item.getItemName()
                );
            }

            BigDecimal unitPrice = itemRequest.getUnitPrice();

            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Unit price is invalid for item: " + item.getItemName()
                );
            }

            BigDecimal otherCharges = itemRequest.getOtherCharges() == null
                    ? BigDecimal.ZERO
                    : itemRequest.getOtherCharges();

            if (otherCharges.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Other charges cannot be negative for item: " + item.getItemName()
                );
            }

            BigDecimal itemSubtotal = unitPrice
                    .multiply(BigDecimal.valueOf(availableQty))
                    .setScale(4, RoundingMode.HALF_UP);

            BigDecimal lineSubtotal = itemSubtotal
                    .add(otherCharges)
                    .setScale(4, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineSubtotal);

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("itemId", item.getItemId());
            line.put("itemName", item.getItemName());
            line.put("requiredQty", item.getReqQty());
            line.put("availableQty", availableQty);
            line.put("uom", item.getUom());
            line.put("unitPrice", unitPrice);
            line.put("otherCharges", otherCharges);
            line.put("itemSubtotal", itemSubtotal);
            line.put("subtotal", lineSubtotal);

            quotationItems.add(line);
        }

        BigDecimal gst = subtotal
                .multiply(GST_RATE)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal grandTotal = subtotal
                .add(gst)
                .setScale(4, RoundingMode.HALF_UP);

        Map<String, Object> storedDetails = new LinkedHashMap<>();
        storedDetails.put("gstRate", 10);
        storedDetails.put("subtotal", subtotal);
        storedDetails.put("gstAmount", gst);
        storedDetails.put("grandTotal", grandTotal);
        storedDetails.put("items", quotationItems);
        storedDetails.put("remarks", request.getRemarks());

        String detailsJson;

        try {
            detailsJson = QUOTATION_JSON_PREFIX
                    + objectMapper.writeValueAsString(storedDetails);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to prepare quotation details", e);
        }

        RFQQuotation quotation = rfqQuotationRepository
                .findByRfq_RfqIdAndVendor_UserId(rfqId, vendorId)
                .orElseGet(RFQQuotation::new);

        quotation.setBidNo(
                quotation.getBidNo() != null
                        ? quotation.getBidNo()
                        : "BID-" + rfq.getRfqNo() + "-V" + vendorId
        );

        quotation.setQuotedAmount(grandTotal);
        quotation.setDeliveryDate(request.getDeliveryDate());
        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setRemarks(detailsJson);
        quotation.setStatus("SUBMITTED");
        quotation.setSubmittedDate(LocalDateTime.now());
        quotation.setRfq(rfq);
        quotation.setVendor(vendor);

        rfqQuotationRepository.save(quotation);
    }

    @Override
    @Transactional
    public List<RFQQuotation> getMySubmissions(Long vendorId) {
        getVendor(vendorId);
        return rfqQuotationRepository
                .findByVendor_UserIdOrderBySubmittedDateDesc(vendorId);
    }

    @Override
    @Transactional
    public RFQQuotation getMySubmission(Long quotationId, Long vendorId) {
        getVendor(vendorId);

        RFQQuotation quotation = rfqQuotationRepository
                .findByQuotationIdAndVendor_UserId(quotationId, vendorId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));

        if (quotation.getRfq() != null && quotation.getRfq().getItems() != null) {
            quotation.getRfq().getItems().size();
        }

        return quotation;
    }

    @Override
    @Transactional
    public List<FinalizedQuotation> getFinalizedQuotations(Long vendorId) {
        getVendor(vendorId);

        List<FinalizedQuotation> finalized =
                finalizedQuotationRepository
                        .findByQuotation_Vendor_UserIdOrderByFinalizedDateDesc(vendorId);

        finalized.forEach(item -> {
            if (item.getRfq() != null && item.getRfq().getItems() != null) {
                item.getRfq().getItems().size();
            }

            if (item.getQuotation() != null) {
                item.getQuotation().getQuotedAmount();
                item.getQuotation().getBidNo();
            }
        });

        return finalized;
    }

    // =========================================================
    // ADMIN - ALL VENDOR QUOTATIONS
    // =========================================================

    @Override
    @Transactional
    public List<RFQQuotation> getAllVendorQuotations() {
        List<RFQQuotation> quotations =
                rfqQuotationRepository.findAllByOrderBySubmittedDateDesc();

        quotations.forEach(quotation -> {
            if (quotation.getRfq() != null && quotation.getRfq().getItems() != null) {
                quotation.getRfq().getItems().size();
            }

            if (quotation.getVendor() != null) {
                quotation.getVendor().getUserId();
            }
        });

        return quotations;
    }

    @Override
    @Transactional
    public RFQQuotation getQuotationForAdmin(Long quotationId) {
        RFQQuotation quotation = rfqQuotationRepository
                .findById(quotationId)
                .orElseThrow(() -> new RuntimeException(
                        "Vendor quotation not found: " + quotationId
                ));

        if (quotation.getRfq() != null && quotation.getRfq().getItems() != null) {
            quotation.getRfq().getItems().size();
        }

        if (quotation.getVendor() != null) {
            quotation.getVendor().getUserId();
        }

        return quotation;
    }

    @Override
    @Transactional
    public void finalizeQuotation(Long quotationId) {
        RFQQuotation quotation = getQuotationForAdmin(quotationId);

        if (finalizedQuotationRepository
                .findByQuotation_QuotationId(quotationId)
                .isPresent()) {
            throw new RuntimeException("This quotation is already finalized.");
        }

        RFQ rfq = quotation.getRfq();

        if (rfq == null) {
            throw new RuntimeException("Quotation is not linked to an RFQ.");
        }

        if (Boolean.TRUE.equals(rfq.getIsDeleted())) {
            throw new RuntimeException("Inactive RFQ cannot be finalized.");
        }

        FinalizedQuotation finalized = new FinalizedQuotation();
        finalized.setFinalizedDate(LocalDateTime.now());
        finalized.setRfq(rfq);
        finalized.setQuotation(quotation);

        quotation.setStatus("FINALIZED");
        rfq.setStatus(RFQStatus.FINALIZED);

        rfqQuotationRepository.save(quotation);
        finalizedQuotationRepository.save(finalized);
    }
}
