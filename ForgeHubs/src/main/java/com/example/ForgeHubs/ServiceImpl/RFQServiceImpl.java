package com.example.ForgeHubs.ServiceImpl;

import com.example.ForgeHubs.DTO.RFQCreateRequest;
import com.example.ForgeHubs.DTO.RFQItemRequest;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQItem;
import com.example.ForgeHubs.Entity.RFQVendor;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Repository.RFQItemRepository;
import com.example.ForgeHubs.Repository.RFQRepository;
import com.example.ForgeHubs.Repository.RFQVendorRepository;
import com.example.ForgeHubs.Repository.UserRepository;
import com.example.ForgeHubs.Service.RFQService;
import com.example.ForgeHubs.enums.RFQStatus;
import com.example.ForgeHubs.enums.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RFQServiceImpl implements RFQService {

    private final RFQRepository rfqRepository;
    private final RFQItemRepository rfqItemRepository;
    private final RFQVendorRepository rfqVendorRepository;
    private final UserRepository userRepository;


    // =========================================================
    // RFQ NUMBER GENERATION
    // =========================================================

    @Override
    public String generateRfqNo() {

        long nextNumber = rfqRepository.count() + 1;

        return String.format(
                "RFQ-%06d",
                nextNumber
        );
    }


    // =========================================================
    // INDENT NUMBER GENERATION
    // =========================================================

    @Override
    public String generateIndentNo() {

        long nextNumber = rfqRepository.count() + 1;

        return String.format(
                "IND-%06d",
                nextNumber
        );
    }


    // =========================================================
    // SAVE RFQ
    // =========================================================

    @Override
    @Transactional
    public void saveRfq(
            RFQCreateRequest request,
            Long adminUserId,
            boolean draft
    ) {

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Admin user not found"
                        )
                );

        RFQ rfq = new RFQ();

        rfq.setRfqNo(generateRfqNo());
        rfq.setIndentNo(generateIndentNo());

        rfq.setContactPerson(
                request.getContactPerson()
        );

        rfq.setMobile(
                request.getMobile()
        );

        if (request.getBidDate() != null) {

            rfq.setBidDate(
                    request.getBidDate().atStartOfDay()
            );
        }

        if (request.getExpiryDateOfBid() != null) {

            rfq.setExpiryDateOfBid(
                    request.getExpiryDateOfBid().atStartOfDay()
            );
        }

        rfq.setStatus(
                draft
                        ? RFQStatus.DRAFT
                        : RFQStatus.OPEN
        );

        rfq.setUser(admin);
        rfq.setIsDeleted(false);


        // -----------------------------------------------------
        // CREATE ITEMS
        // -----------------------------------------------------

        List<RFQItem> items = new ArrayList<>();

        if (request.getItems() != null) {

            int lineNo = 1;

            for (RFQItemRequest itemRequest
                    : request.getItems()) {

                RFQItem item = new RFQItem();

                item.setRfq(rfq);

                // Auto-generated fields
                item.setRfqLineNo(lineNo);

                item.setItemNo(
                        String.format(
                                "ITEM-%06d",
                                lineNo
                        )
                );

                item.setFactoryCode(
                        String.format(
                                "FAC-%06d",
                                lineNo
                        )
                );

                // User-entered fields
                item.setItemName(
                        itemRequest.getItemName()
                );

                item.setReqQty(
                        itemRequest.getReqQty()
                );

                item.setUom(
                        itemRequest.getUom()
                );

                item.setReqDeliveryDate(
                        itemRequest.getReqDeliveryDate()
                );

                item.setDeliveryLocation(
                        itemRequest.getDeliveryLocation()
                );

                item.setDescription(
                        itemRequest.getDescription()
                );

                items.add(item);

                lineNo++;
            }
        }

        rfq.setItems(items);


        // -----------------------------------------------------
        // SAVE RFQ
        // -----------------------------------------------------

        RFQ savedRFQ = rfqRepository.save(rfq);


        // -----------------------------------------------------
        // VENDOR ASSIGNMENT
        // -----------------------------------------------------

        if (request.getVendorIds() != null) {

            for (Long vendorId
                    : request.getVendorIds()) {

                User vendor = userRepository.findById(vendorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vendor not found: "
                                                + vendorId
                                )
                        );

                if (vendor.getRole() != UserRole.VENDOR) {

                    throw new RuntimeException(
                            "Selected user is not a vendor: "
                                    + vendor.getName()
                    );
                }

                RFQVendor rfqVendor =
                        new RFQVendor();

                rfqVendor.setRfq(savedRFQ);
                rfqVendor.setVendor(vendor);

                rfqVendorRepository.save(
                        rfqVendor
                );
            }
        }
    }


    // =========================================================
    // GET ALL VENDORS
    // =========================================================

    @Override
    public List<User> getAllVendors() {

        return userRepository.findByRole(
                UserRole.VENDOR
        );
    }


    // =========================================================
    // GET ALL RFQs
    // =========================================================

//    @Override
//    @Transactional
//    public List<RFQ> getAllRFQs() {
//
//        List<RFQ> rfqs =
//                rfqRepository
//                        .findAllByOrderByRfqIdDesc();
//
//        /*
//         * Initialize items while transaction
//         * is still active.
//         */
//        rfqs.forEach(
//                rfq -> rfq.getItems().size()
//        );
//
//        return rfqs;
//    }
@Override
@Transactional
public List<RFQ> getAllRFQs() {

    List<RFQ> rfqs = rfqRepository.findAllByOrderByRfqIdDesc();

    LocalDateTime now = LocalDateTime.now();

    for (RFQ rfq : rfqs) {

        // Load items
        rfq.getItems().size();

        // Automatically close expired OPEN RFQs
        if (rfq.getStatus() == RFQStatus.OPEN
                && rfq.getExpiryDateOfBid() != null
                && now.isAfter(rfq.getExpiryDateOfBid())) {

            rfq.setStatus(RFQStatus.CLOSED);
            rfqRepository.save(rfq);
        }
    }

    return rfqs;
}


    // =========================================================
    // GET RFQ BY ID
    // =========================================================

    @Override
    public RFQ getRFQById(Integer id) {

        return rfqRepository
                .findByRfqIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "RFQ not found with ID: "
                                        + id
                        )
                );
    }


    // =========================================================
    // GET RFQ ITEMS
    // =========================================================

    @Override
    public List<RFQItem> getRFQItems(
            Integer rfqId
    ) {

        return rfqItemRepository
                .findByRfq_RfqId(rfqId);
    }


    // =========================================================
    // SOFT DELETE
    // =========================================================

    @Override
    @Transactional
    public void softDeleteRFQ(Integer id) {

        RFQ rfq = rfqRepository
                .findByRfqIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "RFQ not found with ID: "
                                        + id
                        )
                );

        rfq.setIsDeleted(true);

        rfqRepository.save(rfq);
    }


    // =========================================================
    // UPDATE RFQ HEADER ONLY
    // =========================================================

    @Override
    @Transactional
    public void updateRFQ(
            Integer id,
            RFQCreateRequest request,
            boolean draft
    ) {

        RFQ rfq = rfqRepository
                .findByRfqIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "RFQ not found with ID: "
                                        + id
                        )
                );


        // -----------------------------------------------------
        // UPDATE ONLY RFQ HEADER
        // -----------------------------------------------------

        rfq.setContactPerson(
                request.getContactPerson()
        );

        rfq.setMobile(
                request.getMobile()
        );

        if (request.getBidDate() != null) {

            rfq.setBidDate(
                    request.getBidDate().atStartOfDay()
            );
        }

        if (request.getExpiryDateOfBid() != null) {

            rfq.setExpiryDateOfBid(
                    request.getExpiryDateOfBid().atStartOfDay()
            );
        }


        /*
         * IMPORTANT:
         *
         * Do NOT clear RFQ items here.
         *
         * Outer "Edit RFQ" is only for:
         *
         * Contact Person
         * Mobile
         * Bid Start Date
         * Bid End Date
         *
         * Existing products must remain unchanged.
         */


        /*
         * Do NOT delete/recreate vendors here either.
         *
         * Existing vendor assignments remain unchanged.
         */


        /*
         * Preserve existing RFQ status.
         *
         * Editing the header should NOT automatically
         * change CLOSED / REOPENED / DRAFT etc.
         */
        rfqRepository.save(rfq);
    }


    // =========================================================
    // UPDATE SINGLE RFQ ITEM
    // =========================================================

    @Override
    @Transactional
    public void updateRFQItem(
            Integer itemId,
            String itemName,
            Integer reqQty,
            String uom,
            LocalDate reqDeliveryDate,
            String deliveryLocation,
            String description
    ) {

        RFQItem item = rfqItemRepository
                .findById(itemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "RFQ Item not found with ID: "
                                        + itemId
                        )
                );


        /*
         * Only editable product fields
         * are updated.
         */

        item.setItemName(itemName);

        item.setReqQty(reqQty);

        item.setUom(uom);

        item.setReqDeliveryDate(
                reqDeliveryDate
        );

        item.setDeliveryLocation(
                deliveryLocation
        );

        item.setDescription(
                description
        );


        /*
         * DO NOT modify:
         *
         * ItemId
         * RFQ
         * RFQLineNo
         * ItemNo
         * FactoryCode
         */


        rfqItemRepository.save(item);
    }


    // =========================================================
    // OPEN RFQ TO REBID
    // =========================================================

    @Override
    @Transactional
    public void openToRebid(Integer id) {

        RFQ rfq = rfqRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "RFQ not found with ID: "
                                        + id
                        )
                );

        if (Boolean.TRUE.equals(
                rfq.getIsDeleted()
        )) {

            throw new RuntimeException(
                    "Inactive RFQ cannot be opened for rebid."
            );
        }

        if (rfq.getStatus()
                != RFQStatus.CLOSED) {

            throw new RuntimeException(
                    "Only CLOSED RFQ can be opened for rebid."
            );
        }

        rfq.setStatus(
                RFQStatus.REOPENED
        );

        rfqRepository.save(rfq);
    }
}