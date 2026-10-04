package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.VendorQuotationRequest;
import com.example.ForgeHubs.Entity.FinalizedQuotation;
import com.example.ForgeHubs.Entity.RFQ;
import com.example.ForgeHubs.Entity.RFQQuotation;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Service.VendorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private static final String QUOTATION_JSON_PREFIX = "FORGEHUB_QUOTATION_V1:";

    private final VendorService vendorService;
    private final ObjectMapper objectMapper;

    @GetMapping({"", "/dashboard"})
    public String dashboard(
            @RequestParam Long vendorId,
            Model model
    ) {
        Long id = vendorId;
        User vendor = vendorService.getVendor(id);
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
            @PathVariable Integer rfqId,
            @RequestParam Long vendorId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long id = vendorId;
        try {
            RFQ rfq = vendorService.getAssignedRfq(rfqId, id);
            model.addAttribute("vendor", vendorService.getVendor(id));
            model.addAttribute("rfq", rfq);
            model.addAttribute("vendorId", id);
            model.addAttribute("quotationRequest", new VendorQuotationRequest());
            return "vendor/rfq-details";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/open-rfq?vendorId=" + id;
        }
    }

    @PostMapping("/rfq/{rfqId}/submit")
    public String submitQuotation(
            @PathVariable Integer rfqId,
            @RequestParam Long vendorId,
            @ModelAttribute("quotationRequest") VendorQuotationRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            vendorService.submitQuotation(rfqId, vendorId, request);
            redirectAttributes.addFlashAttribute("success", "Quotation submitted successfully.");
            return "redirect:/vendor/my-submission?vendorId=" + vendorId;
        } catch (RuntimeException e) {
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

        User vendor = vendorService.getVendor(id);

        List<RFQQuotation> submissions =
                vendorService.getMySubmissions(id);

        List<FinalizedQuotation> finalizedQuotations =
                vendorService.getFinalizedQuotations(id);

        /*
         * quotationId -> finalizedDate
         *
         * This is used only for the History popup.
         * No new database table/column is required.
         */
        Map<Integer, java.time.LocalDateTime> finalizedDates =
                new java.util.HashMap<>();

        for (FinalizedQuotation finalized : finalizedQuotations) {

            if (finalized.getQuotation() != null) {

                finalizedDates.put(
                        finalized.getQuotation().getQuotationId(),
                        finalized.getFinalizedDate()
                );
            }
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("submissions", submissions);
        model.addAttribute("finalizedDates", finalizedDates);
        model.addAttribute("vendorId", id);

        return "vendor/my-submission";
    }

    @GetMapping("/submission/{quotationId}")
    public String submissionDetails(
            @PathVariable Integer quotationId,
            @RequestParam Long vendorId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
       Long id = vendorId;
        try {
            RFQQuotation quotation = vendorService.getMySubmission(quotationId, id);
            model.addAttribute("vendor", vendorService.getVendor(id));
            model.addAttribute("quotation", quotation);
            model.addAttribute("vendorId", id);
            addStoredQuotationDetails(model, quotation);
            return "vendor/submission-details";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/my-submission?vendorId=" + id;
        }
    }

    private void addStoredQuotationDetails(Model model, RFQQuotation quotation) {
        List<Map<String, Object>> quotationItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstAmount = BigDecimal.ZERO;
        BigDecimal grandTotal = quotation.getQuotedAmount() == null
                ? BigDecimal.ZERO
                : quotation.getQuotedAmount();
        String remarks = "";

        String stored = quotation.getRemarks();
        if (stored != null && stored.startsWith(QUOTATION_JSON_PREFIX)) {
            try {
                JsonNode root = objectMapper.readTree(
                        stored.substring(QUOTATION_JSON_PREFIX.length())
                );
                if (root.has("subtotal")) subtotal = root.get("subtotal").decimalValue();
                if (root.has("gstAmount")) gstAmount = root.get("gstAmount").decimalValue();
                if (root.has("grandTotal")) grandTotal = root.get("grandTotal").decimalValue();
                if (root.has("remarks") && !root.get("remarks").isNull()) {
                    remarks = root.get("remarks").asText();
                }
                if (root.has("items") && root.get("items").isArray()) {
                    for (JsonNode item : root.get("items")) {
                        quotationItems.add(objectMapper.convertValue(item, Map.class));
                    }
                }
            } catch (Exception ignored) {
                remarks = stored;
            }
        } else if (stored != null) {
            remarks = stored;
        }

        model.addAttribute("quotationItems", quotationItems);
        model.addAttribute("quotationSubtotal", subtotal);
        model.addAttribute("quotationGstAmount", gstAmount);
        model.addAttribute("quotationGrandTotal", grandTotal);
        model.addAttribute("quotationRemarks", remarks);
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
