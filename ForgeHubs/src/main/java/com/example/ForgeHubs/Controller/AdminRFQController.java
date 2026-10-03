package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.RFQCreateRequest;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQItem;
import com.example.ForgeHubs.Service.RFQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/rfq")
@RequiredArgsConstructor
public class AdminRFQController {

    private final RFQService rfqService;

    // Temporary admin ID until authentication branch is merged.
    private static final Integer TEST_ADMIN_ID = 1;

    // =========================================================
    // ADD RFQ
    // =========================================================

    @GetMapping("/add")
    public String showAddRFQ(Model model) {

        model.addAttribute("rfqRequest", new RFQCreateRequest());
        model.addAttribute("vendors", rfqService.getAllVendors());

        return "admin/add-rfq";
    }

    // =========================================================
    // SAVE RFQ
    // =========================================================

    @PostMapping("/save")
    public String saveRFQ(
            @ModelAttribute("rfqRequest") RFQCreateRequest request,
            @RequestParam(name = "action", defaultValue = "submit") String action,
            Model model
    ) {

        boolean draft = action.equalsIgnoreCase("draft");

        try {
            rfqService.saveRfq(request, TEST_ADMIN_ID, draft);
            return "redirect:/admin/rfq/list";

        } catch (Exception e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("vendors", rfqService.getAllVendors());

            return "admin/add-rfq";
        }
    }

    // =========================================================
    // RFQ LIST
    // =========================================================

    @GetMapping("/list")
    public String showRFQList(Model model) {

        model.addAttribute("rfqs", rfqService.getAllRFQs());

        return "admin/rfq-list";
    }

    // =========================================================
    // RFQ DETAILS FOR VIEW MODAL
    // =========================================================

    @GetMapping("/details/{id}")
    @ResponseBody
    public RFQDetailsResponse getRFQDetails(
            @PathVariable Integer id
    ) {

        RFQ rfq = rfqService.getRFQById(id);

        List<RFQItem> items = rfqService.getRFQItems(id);

        List<RFQItemResponse> itemResponses = items.stream()
                .map(item -> new RFQItemResponse(
                        item.getItemId(),
                        item.getRfqLineNo(),
                        item.getItemNo(),
                        item.getItemName(),
                        item.getReqQty(),
                        item.getUom(),
                        item.getReqDeliveryDate(),
                        item.getDeliveryLocation(),
                        item.getFactoryCode(),
                        item.getDescription()
                ))
                .toList();

        return new RFQDetailsResponse(
                rfq.getRfqId(),
                rfq.getRfqNo(),
                rfq.getIndentNo(),
                rfq.getContactPerson(),
                rfq.getMobile(),
                rfq.getBidDate(),
                rfq.getExpiryDateOfBid(),
                rfq.getStatus() != null ? rfq.getStatus().name() : null,
                Boolean.TRUE.equals(rfq.getIsDeleted()),
                itemResponses
        );
    }

    // =========================================================
    // UPDATE RFQ HEADER
    // =========================================================

    @PostMapping("/update/{id}")
    public String updateRFQ(
            @PathVariable Integer id,
            @ModelAttribute RFQCreateRequest request,
            @RequestParam(name = "action", defaultValue = "submit") String action
    ) {

        boolean draft = action.equalsIgnoreCase("draft");

        rfqService.updateRFQ(id, request, draft);

        return "redirect:/admin/rfq/list";
    }

    // =========================================================
    // UPDATE SINGLE RFQ ITEM
    // =========================================================

    @PostMapping("/item/update")
    @ResponseBody
    public ResponseEntity<String> updateRFQItem(

            @RequestParam Integer itemId,
            @RequestParam String itemName,
            @RequestParam Integer reqQty,
            @RequestParam String uom,
            @RequestParam String reqDeliveryDate,
            @RequestParam String deliveryLocation,
            @RequestParam(required = false, defaultValue = "") String description
    ) {

        rfqService.updateRFQItem(
                itemId,
                itemName,
                reqQty,
                uom,
                LocalDate.parse(reqDeliveryDate),
                deliveryLocation,
                description
        );

        return ResponseEntity.ok("success");
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    @PostMapping("/delete/{id}")
    public String deleteRFQ(@PathVariable Integer id) {

        rfqService.softDeleteRFQ(id);

        return "redirect:/admin/rfq/list";
    }

    // =========================================================
    // OPEN TO REBID
    // =========================================================

    @PostMapping("/rebid/{id}")
    public String openToRebid(@PathVariable Integer id) {

        rfqService.openToRebid(id);

        return "redirect:/admin/rfq/list";
    }

    // =========================================================
    // RESPONSE DTOs
    // =========================================================

    public record RFQDetailsResponse(
            Integer rfqId,
            String rfqNo,
            String indentNo,
            String contactPerson,
            String mobile,
            java.time.LocalDateTime bidDate,
            java.time.LocalDateTime expiryDateOfBid,
            String status,
            boolean deleted,
            List<RFQItemResponse> items
    ) {
    }

    public record RFQItemResponse(
            Integer itemId,
            Integer rfqLineNo,
            String itemNo,
            String itemName,
            Integer reqQty,
            String uom,
            LocalDate reqDeliveryDate,
            String deliveryLocation,
            String factoryCode,
            String description
    ) {
    }
}
