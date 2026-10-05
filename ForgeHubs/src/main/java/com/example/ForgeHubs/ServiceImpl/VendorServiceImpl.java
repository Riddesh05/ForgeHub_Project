        package com.example.ForgeHubs.ServiceImpl;

        import com.example.ForgeHubs.DTO.*;
        import com.example.ForgeHubs.Entity.*;
        import com.example.ForgeHubs.Repository.FinalizedQuotationRepository;
        import com.example.ForgeHubs.Repository.RFQQuotationRepository;
        import com.example.ForgeHubs.Repository.RFQVendorRepository;
        import com.example.ForgeHubs.Repository.UserRepository;
        import com.example.ForgeHubs.Service.VendorService;
        import com.example.ForgeHubs.Util.QuotationResponseMapper;
        import com.example.ForgeHubs.enums.RFQStatus;
        import com.example.ForgeHubs.enums.UserRole;
        import com.example.ForgeHubs.exception.BusinessException;
        import com.example.ForgeHubs.exception.ResourceNotFoundException;
        import com.fasterxml.jackson.core.JsonProcessingException;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import jakarta.transaction.Transactional;
        import lombok.RequiredArgsConstructor;
        import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;
    private final QuotationResponseMapper quotationResponseMapper;

    @Override
    @Transactional
    public UserResponseDto getVendor(Long vendorId) {

        User vendor = getVendorEntity(vendorId);
        return modelMapper.map(vendor, UserResponseDto.class);
    }

    private User getVendorEntity(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor was not found. Please select a valid vendor."
                ));

        if (vendor.getRole() != UserRole.VENDOR) {
            throw new BusinessException(
                    "Selected user is not a vendor: " + vendor.getName()
            );
        }

        return vendor;
    }

    @Override
    @Transactional
    public List<RFQResponseDto> getOpenRfqs(Long vendorId) {

        getVendorEntity(vendorId);

        List<RFQ> rfqs = rfqVendorRepository.findActiveAssignedRfqs(vendorId);
        LocalDate today = LocalDate.now();

        return rfqs.stream()
                .filter(rfq -> rfq.getExpiryDateOfBid() == null
                        || !today.isAfter(rfq.getExpiryDateOfBid().toLocalDate()))
                .peek(rfq -> {
                    if (rfq.getItems() != null) {
                        rfq.getItems().size();
                    }
                })
                .map(rfq -> modelMapper.map(rfq, RFQResponseDto.class))
                .toList();
    }

    @Override
    @Transactional
    public RFQResponseDto getAssignedRfq(Long rfqId, Long vendorId) {
        return modelMapper.map(
                getAssignedRfqEntity(rfqId, vendorId),
                RFQResponseDto.class
        );
    }

    private RFQ getAssignedRfqEntity(Long rfqId, Long vendorId) {

        getVendorEntity(vendorId);

        if (!rfqVendorRepository.existsByRfq_RfqIdAndVendor_UserId(rfqId, vendorId)) {
            throw new BusinessException(
                    "This RFQ is not assigned to the selected vendor"
            );
        }

        RFQ rfq = rfqVendorRepository
                .findByRfq_RfqIdAndVendor_UserId(rfqId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RFQ assignment was not found for this vendor."
                ))
                .getRfq();

        if (Boolean.TRUE.equals(rfq.getIsDeleted())) {
            throw new BusinessException("This RFQ is inactive");
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
        User vendor = getVendorEntity(vendorId);
        RFQ rfq = getAssignedRfqEntity(rfqId, vendorId);

        if (rfq.getStatus() != RFQStatus.OPEN && rfq.getStatus() != RFQStatus.REOPENED) {
            throw new BusinessException("Quotation can only be submitted for an OPEN or REOPENED RFQ");
        }

        if (rfq.getExpiryDateOfBid() != null
                && LocalDate.now().isAfter(rfq.getExpiryDateOfBid().toLocalDate())) {
            throw new BusinessException("Bid submission date has expired");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Quotation must contain at least one item");
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
                throw new BusinessException("Invalid RFQ item selected: " + itemRequest.getItemId());
            }

            int availableQty = itemRequest.getAvailableQty() == null
                    ? 0
                    : itemRequest.getAvailableQty();

            if (availableQty < 0
                    || (item.getReqQty() != null && availableQty > item.getReqQty())) {
                throw new BusinessException(
                        "Available quantity is invalid for item: " + item.getItemName()
                );
            }

            BigDecimal unitPrice = itemRequest.getUnitPrice();

            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                        "Unit price is invalid for item: " + item.getItemName()
                );
            }


            BigDecimal itemSubtotal = unitPrice
                    .multiply(BigDecimal.valueOf(availableQty))
                    .setScale(4, RoundingMode.HALF_UP);

            BigDecimal lineSubtotal = itemSubtotal;

            subtotal = subtotal.add(lineSubtotal);

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("itemId", item.getItemId());
            line.put("itemName", item.getItemName());
            line.put("requiredQty", item.getReqQty());
            line.put("availableQty", availableQty);
            line.put("uom", item.getUom());
            line.put("unitPrice", unitPrice);
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

        String detailsJson;

        try {
            detailsJson = QUOTATION_JSON_PREFIX
                    + objectMapper.writeValueAsString(storedDetails);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Unable to prepare quotation details", e);
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
    public List<VendorQuotationResponseDto> getMySubmissions(Long vendorId) {

        getVendorEntity(vendorId);

        return rfqQuotationRepository
                .findByVendor_UserIdOrderBySubmittedDateDesc(vendorId)
                .stream()
                .map(quotationResponseMapper::toVendorQuotationResponse)
                .toList();
    }

    @Override
    @Transactional
    public VendorQuotationResponseDto getMySubmission(
            Long quotationId,
            Long vendorId
    ) {

        getVendorEntity(vendorId);

        RFQQuotation quotation = rfqQuotationRepository
                .findByQuotationIdAndVendor_UserId(quotationId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quotation was not found."
                ));

        if (quotation.getRfq() != null && quotation.getRfq().getItems() != null) {
            quotation.getRfq().getItems().size();
        }

        return quotationResponseMapper.toVendorQuotationResponse(quotation);
    }

    @Override
    @Transactional
    public List<FinalizedQuotationResponseDto> getFinalizedQuotations(Long vendorId) {

        getVendorEntity(vendorId);

        return finalizedQuotationRepository
                .findByQuotation_Vendor_UserIdOrderByFinalizedDateDesc(vendorId)
                .stream()
                .peek(item -> {
                    if (item.getRfq() != null && item.getRfq().getItems() != null) {
                        item.getRfq().getItems().size();
                    }
                })
                .map(quotationResponseMapper::toFinalizedQuotationResponse)
                .toList();
    }

    // =========================================================
    // ADMIN - ALL VENDOR QUOTATIONS
    // =========================================================

    @Override
    @Transactional
    public List<VendorQuotationResponseDto> getAllVendorQuotations() {

        return rfqQuotationRepository.findAllByOrderBySubmittedDateDesc()
                .stream()
                .peek(quotation -> {
                    if (quotation.getRfq() != null && quotation.getRfq().getItems() != null) {
                        quotation.getRfq().getItems().size();
                    }
                })
                .map(quotationResponseMapper::toVendorQuotationResponse)
                .toList();
    }

    @Override
    @Transactional
    public VendorQuotationResponseDto getQuotationForAdmin(Long quotationId) {
        return quotationResponseMapper.toVendorQuotationResponse(
                getQuotationEntityForAdmin(quotationId)
        );
    }

    private RFQQuotation getQuotationEntityForAdmin(Long quotationId) {

        RFQQuotation quotation = rfqQuotationRepository
                .findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor quotation was not found."
                ));

        if (quotation.getRfq() != null && quotation.getRfq().getItems() != null) {
            quotation.getRfq().getItems().size();
        }

        return quotation;
    }

    @Override
    @Transactional
    public void finalizeQuotation(Long quotationId) {

        // 1. Selected quotation find karo
        RFQQuotation selectedQuotation = getQuotationEntityForAdmin(quotationId);

        // 2. Check already finalized
        if (finalizedQuotationRepository
                .findByQuotation_QuotationId(quotationId)
                .isPresent()) {

            throw new BusinessException(
                    "This quotation is already finalized."
            );
        }

        // 3. RFQ check
        RFQ rfq = selectedQuotation.getRfq();

        if (rfq == null) {
            throw new BusinessException(
                    "Quotation is not linked to an RFQ."
            );
        }

        // 4. Deleted / inactive RFQ cannot be finalized
        if (Boolean.TRUE.equals(rfq.getIsDeleted())) {
            throw new BusinessException(
                    "Inactive RFQ cannot be finalized."
            );
        }

        // 5. RFQ already finalized hai toh dobara finalize mat karo
        if (rfq.getStatus() == RFQStatus.FINALIZED) {
            throw new BusinessException(
                    "This RFQ has already been finalized."
            );
        }

        // =========================================================
        // 6. SAME RFQ KI SAARI QUOTATIONS NIKALO
        // =========================================================

        List<RFQQuotation> quotations =
                rfqQuotationRepository.findByRfq_RfqId(
                        rfq.getRfqId()
                );

        // =========================================================
        // 7. SELECTED = FINALIZED
        //    OTHERS = REJECTED
        // =========================================================

        for (RFQQuotation quotation : quotations) {

            if (quotation.getQuotationId().equals(quotationId)) {

                // Admin ne jis vendor ko select kiya
                quotation.setStatus("FINALIZED");

            } else {

                // Same RFQ ke baaki vendors
                quotation.setStatus("REJECTED");
            }

            rfqQuotationRepository.save(quotation);
        }

        // =========================================================
        // 8. RFQ STATUS = FINALIZED
        // =========================================================

        rfq.setStatus(RFQStatus.FINALIZED);

        // =========================================================
        // 9. FINALIZED QUOTATION RECORD CREATE KARO
        // =========================================================

        FinalizedQuotation finalized = new FinalizedQuotation();

        finalized.setFinalizedDate(LocalDateTime.now());
        finalized.setRfq(rfq);
        finalized.setQuotation(selectedQuotation);

        finalizedQuotationRepository.save(finalized);
    }

//    @Override
//    @Transactional
//    public List<FinalizedQuotationResponseDto>  getAllFinalizedQuotations() {
//        return finalizedQuotationRepository.findAllByOrderByFinalizedDateDesc()
//                .stream()
//                .peek(item -> {
//                    if (item.getRfq() != null && item.getRfq().getItems() != null) {
//                        item.getRfq().getItems().size();
//                    }
//                })
//                .map(quotationResponseMapper::toFinalizedQuotationResponse)
//                .toList();
//    }

    @Override
    public List<FinalizedQuotationResponseDto> getAllFinalizedQuotations() {

        return finalizedQuotationRepository
                .findAllByOrderByFinalizedDateDesc()
                .stream()
                .map(quotationResponseMapper::toFinalizedQuotationResponse)
                .toList();
    }
}
