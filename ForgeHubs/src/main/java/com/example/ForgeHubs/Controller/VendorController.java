package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.*;
import com.example.ForgeHubs.Service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(
            @RequestParam Long vendorId,
            Model model
    ) {
        Long id = vendorId;
        UserResponseDto vendor = vendorService.getVendor(id);
        model.addAttribute("vendor", vendor);
        return "redirect:/vendor/open-rfq?vendorId=" + id;
    }

    @GetMapping("/open-rfq")
    public String openRfqs(
            @RequestParam Long vendorId,
            Model model
    ) {
        Long id = vendorId;
        model.addAttribute("vendor", vendorService.getVendor(id));
        model.addAttribute("rfqs", vendorService.getOpenRfqs(id));
        model.addAttribute("vendorId", id);
        return "vendor/open-rfq";
    }

    @GetMapping({"/rfq/{rfqId}", "/quote/{rfqId}"})
    public String rfqDetails(
            @PathVariable Long rfqId,
            @RequestParam Long vendorId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long id = vendorId;
        try {
            RFQResponseDto rfq = vendorService.getAssignedRfq(rfqId, id);
            model.addAttribute("vendor", vendorService.getVendor(id));
            model.addAttribute("rfq", rfq);
            model.addAttribute("vendorId", id);
            model.addAttribute("quotationRequest", new VendorQuotationRequest());
            return "vendor/rfq-details";
        } catch (RuntimeException e) {
            log.warn("Request failed: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/open-rfq?vendorId=" + id;
        }
    }

    @PostMapping("/rfq/{rfqId}/submit")
    public String submitQuotation(
            @PathVariable Long rfqId,
            @RequestParam Long vendorId,
            @ModelAttribute("quotationRequest") VendorQuotationRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            vendorService.submitQuotation(rfqId, vendorId, request);
            redirectAttributes.addFlashAttribute("success", "Quotation submitted successfully.");
            return "redirect:/vendor/my-submission?vendorId=" + vendorId;
        } catch (RuntimeException e) {
            log.warn("Request failed: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/rfq/" + rfqId + "?vendorId=" + vendorId;
        }
    }

    @GetMapping("/my-submission")
    public String mySubmissions(
            @RequestParam Long vendorId,
            Model model
    ) {
        Long id = vendorId;

        UserResponseDto vendor = vendorService.getVendor(id);

        List<VendorQuotationResponseDto> submissionResponses =
                new ArrayList<>(vendorService.getMySubmissions(id));

        List<FinalizedQuotationResponseDto> finalizedQuotations =
                vendorService.getFinalizedQuotations(id);

        Map<Long, java.time.LocalDateTime> finalizedDates = new java.util.HashMap<>();
        for (FinalizedQuotationResponseDto finalized : finalizedQuotations) {
            Long quotationId = finalized.getQuotationId();
            if (quotationId != null) {
                finalizedDates.put(quotationId, finalized.getFinalizedDate());

                submissionResponses.stream()
                        .filter(s -> quotationId.equals(s.getQuotationId()))
                        .findFirst()
                        .ifPresent(s -> {
                            if (s.getHistory() != null) {
                                s.getHistory().add(new com.example.ForgeHubs.DTO.VendorQuotationHistory(
                                        "Quotation Finalized",
                                        "FINAL",
                                        s.getQuotedAmount(),
                                        finalized.getFinalizedDate(),
                                        "FINALIZED"
                                ));
                            }
                        });
            }
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("submissions", submissionResponses);
        model.addAttribute("finalizedDates", finalizedDates);
        model.addAttribute("vendorId", id);

        return "vendor/my-submission";
    }

    @GetMapping("/submission/{quotationId}")
    public String submissionDetails(
            @PathVariable Long quotationId,
            @RequestParam Long vendorId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long id = vendorId;
        try {
            VendorQuotationResponseDto quotation = vendorService.getMySubmission(quotationId, id);
            model.addAttribute("vendor", vendorService.getVendor(id));
            model.addAttribute("quotation", quotation);
            model.addAttribute("vendorId", id);
            addStoredQuotationDetails(model, quotation);
            return "vendor/submission-details";
        } catch (RuntimeException e) {
            log.warn("Request failed: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/my-submission?vendorId=" + id;
        }
    }

    private void addStoredQuotationDetails(
            Model model,
            VendorQuotationResponseDto quotation
    ) {

        List<Map<String, Object>> quotationItems = new ArrayList<>();

        if (quotation.getItems() != null) {
            quotation.getItems().forEach(item -> {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("itemId", item.getItemId());
                row.put("itemName", item.getItemName());
                row.put("requiredQty", item.getRequiredQty());
                row.put("availableQty", item.getAvailableQty());
                row.put("uom", item.getUom());
                row.put("unitPrice", item.getUnitPrice());
                row.put("subtotal", item.getItemSubtotal() != null
                        ? item.getItemSubtotal()
                        : item.getSubtotal());
                quotationItems.add(row);
            });
        }

        model.addAttribute("quotationItems", quotationItems);
        model.addAttribute(
                "quotationSubtotal",
                quotation.getSubtotal() == null
                        ? BigDecimal.ZERO
                        : quotation.getSubtotal()
        );
        model.addAttribute(
                "quotationGstAmount",
                quotation.getGstAmount() == null
                        ? BigDecimal.ZERO
                        : quotation.getGstAmount()
        );
        model.addAttribute(
                "quotationGrandTotal",
                quotation.getGrandTotal() == null
                        ? BigDecimal.ZERO
                        : quotation.getGrandTotal()
        );

    }

    @GetMapping("/finalized-quotation")
    public String finalizedQuotations(
            @RequestParam Long vendorId,
            Model model
    ) {
        Long id = vendorId;
        model.addAttribute("vendor", vendorService.getVendor(id));
        model.addAttribute("finalizedQuotations", vendorService.getFinalizedQuotations(id));
        model.addAttribute("vendorId", id);
        return "vendor/finalized-quotation";
    }
}
