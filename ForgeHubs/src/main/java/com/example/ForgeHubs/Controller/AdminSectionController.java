package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.UserCreateRequest;
import com.example.ForgeHubs.Entity.RFQQuotation;
import com.example.ForgeHubs.Service.UserService;
import com.example.ForgeHubs.Service.VendorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminSectionController {

    private final UserService userService;
    private final VendorService vendorService;
    private final ObjectMapper objectMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Dashboard");
        return "admin/section-placeholder";
    }

    @GetMapping("/vendor-quotation")
    public String vendorQuotation(Model model) {
        model.addAttribute("quotations", vendorService.getAllVendorQuotations());
        return "admin/vendor-quotation";
    }

    @GetMapping("/vendor-quotation/details/{quotationId}")
    @ResponseBody
    public ResponseEntity<VendorQuotationDetailsResponse> vendorQuotationDetails(
            @PathVariable Integer quotationId
    ) {
        RFQQuotation quotation = vendorService.getQuotationForAdmin(quotationId);
        return ResponseEntity.ok(toDetailsResponse(quotation));
    }

    @PostMapping("/vendor-quotation/finalize/{quotationId}")
    public String finalizeVendorQuotation(
            @PathVariable Integer quotationId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            vendorService.finalizeQuotation(quotationId);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Vendor quotation finalized successfully."
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/vendor-quotation";
    }

    private VendorQuotationDetailsResponse toDetailsResponse(RFQQuotation quotation) {
        List<QuotationItemResponse> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstAmount = BigDecimal.ZERO;
        BigDecimal grandTotal = quotation.getQuotedAmount() == null
                ? BigDecimal.ZERO
                : quotation.getQuotedAmount();
        String remarks = "";

        String storedRemarks = quotation.getRemarks();

        if (storedRemarks != null && storedRemarks.startsWith("FORGEHUB_QUOTATION_V1:")) {
            String json = storedRemarks.substring("FORGEHUB_QUOTATION_V1:".length());
            try {
                JsonNode root = objectMapper.readTree(json);

                if (root.has("subtotal")) {
                    subtotal = root.get("subtotal").decimalValue();
                }
                if (root.has("gstAmount")) {
                    gstAmount = root.get("gstAmount").decimalValue();
                }
                if (root.has("grandTotal")) {
                    grandTotal = root.get("grandTotal").decimalValue();
                }
                if (root.has("remarks") && !root.get("remarks").isNull()) {
                    remarks = root.get("remarks").asText();
                }

                JsonNode itemNodes = root.get("items");
                if (itemNodes != null && itemNodes.isArray()) {
                    for (JsonNode item : itemNodes) {
                        items.add(new QuotationItemResponse(
                                item.path("itemId").asInt(),
                                item.path("itemName").asText("-"),
                                item.path("requiredQty").asInt(),
                                item.path("availableQty").asInt(),
                                item.path("uom").asText("-"),
                                item.path("unitPrice").decimalValue(),
                                item.path("otherCharges").decimalValue(),
                                item.path("subtotal").decimalValue()
                        ));
                    }
                }
            } catch (JsonProcessingException e) {
                remarks = storedRemarks;
            }
        } else if (storedRemarks != null) {
            remarks = storedRemarks;
        }

        return new VendorQuotationDetailsResponse(
                quotation.getQuotationId(),
                quotation.getBidNo(),
                quotation.getRfq() != null ? quotation.getRfq().getRfqNo() : "-",
                quotation.getVendor() != null ? quotation.getVendor().getName() : "-",
                quotation.getVendor() != null ? quotation.getVendor().getEmail() : "-",
                grandTotal,
                subtotal,
                gstAmount,
                quotation.getDeliveryDate(),
                quotation.getPaymentTerms(),
                remarks,
                quotation.getStatus(),
                quotation.getSubmittedDate(),
                items
        );
    }

    @GetMapping("/finance-quotation")
    public String financeQuotation(Model model) {
        model.addAttribute("title", "Finance Quotation");
        return "admin/section-placeholder";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("userRequest", new UserCreateRequest());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    public record VendorQuotationDetailsResponse(
            Integer quotationId,
            String bidNo,
            String rfqNo,
            String vendorName,
            String vendorEmail,
            BigDecimal grandTotal,
            BigDecimal subtotal,
            BigDecimal gstAmount,
            java.time.LocalDate deliveryDate,
            String paymentTerms,
            String remarks,
            String status,
            java.time.LocalDateTime submittedDate,
            List<QuotationItemResponse> items
    ) {}

    public record QuotationItemResponse(
            Integer itemId,
            String itemName,
            Integer requiredQty,
            Integer availableQty,
            String uom,
            BigDecimal unitPrice,
            BigDecimal otherCharges,
            BigDecimal subtotal
    ) {}
}
