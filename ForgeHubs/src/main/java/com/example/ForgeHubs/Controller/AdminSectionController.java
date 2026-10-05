package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.RFQResponseDto;
import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.VendorQuotationResponseDto;
import com.example.ForgeHubs.Service.RFQService;
import com.example.ForgeHubs.Service.UserService;
import com.example.ForgeHubs.Service.VendorService;
import com.example.ForgeHubs.enums.RFQStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminSectionController {

    private final UserService userService;
    private final VendorService vendorService;
    private final RFQService rfqService;

    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        List<RFQResponseDto> rfqs = rfqService.getAllRFQs();

        long totalRFQs = rfqs.size();
        long draftRFQs = rfqs.stream()
                .filter(rfq -> rfq.getStatus() == RFQStatus.DRAFT)
                .count();

        long openRFQs = rfqs.stream()
                .filter(rfq ->
                        rfq.getStatus() == RFQStatus.OPEN ||
                                rfq.getStatus() == RFQStatus.REOPENED
                )
                .count();

        long closedRFQs = rfqs.stream()
                .filter(rfq -> rfq.getStatus() == RFQStatus.CLOSED)
                .count();

        long finalizedRFQs = rfqs.stream()
                .filter(rfq -> rfq.getStatus() == RFQStatus.FINALIZED)
                .count();

        model.addAttribute("totalRFQs", totalRFQs);
        model.addAttribute("draftRFQs", draftRFQs);
        model.addAttribute("openRFQs", openRFQs);
        model.addAttribute("closedRFQs", closedRFQs);
        model.addAttribute("finalizedRFQs", finalizedRFQs);
        model.addAttribute("recentRFQs", rfqs.stream().limit(5).toList());

        return "admin/dashboard";
    }

    // =========================================================
    // VENDOR QUOTATION
    // =========================================================

    @GetMapping("/vendor-quotation")
    public String vendorQuotation(Model model) {

        model.addAttribute(
                "quotations",
                vendorService.getAllVendorQuotations()
        );

        return "admin/vendor-quotation";
    }

    // =========================================================
    // VENDOR QUOTATION DETAILS
    // =========================================================

    @GetMapping("/vendor-quotation/details/{quotationId}")
    @ResponseBody
    public ResponseEntity<VendorQuotationResponseDto> vendorQuotationDetails(
            @PathVariable Long quotationId
    ) {
        return ResponseEntity.ok(
                vendorService.getQuotationForAdmin(quotationId)
        );
    }

    // =========================================================
    // FINALIZE VENDOR QUOTATION
    // =========================================================

    @PostMapping("/vendor-quotation/finalize/{quotationId}")
    public String finalizeVendorQuotation(
            @PathVariable Long quotationId,
            RedirectAttributes redirectAttributes
    ) {

        try {
            vendorService.finalizeQuotation(quotationId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Vendor quotation finalized successfully."
            );

        } catch (RuntimeException e) {
            log.warn("Request failed: {}", e.getMessage(), e);

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/vendor-quotation";
    }

    // =========================================================
    // FINANCE QUOTATION
    // =========================================================

    @GetMapping("/finance-quotation")
    public String financeQuotation(Model model) {

        //model.addAttribute("title", "Finance Quotation");
        model.addAttribute(
                "quotations",
                vendorService.getAllFinalizedQuotations()
        );
        return "admin/finance-quotation";
    }

    // =========================================================
    // USERS PAGE
    // =========================================================

    @GetMapping("/users")
    public String users(Model model) {

        model.addAttribute(
                "userRequest",
                new UserRequestDto()
        );

        model.addAttribute(
                "users",
                userService.getAllUsers()
        );

        return "admin/users";
    }



    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {

        ResponseCookie deleteCookie = ResponseCookie.from(
                        "FORGEHUB_ACCESS_TOKEN",
                        ""
                )
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(
                "Set-Cookie",
                deleteCookie.toString()
        );

        return "redirect:/";
    }
}
