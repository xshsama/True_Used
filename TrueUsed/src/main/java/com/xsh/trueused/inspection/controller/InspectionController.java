package com.xsh.trueused.inspection.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.api.ApiResponse;
import com.xsh.trueused.inspection.dto.InspectionFlowDTO;
import com.xsh.trueused.security.user.UserPrincipal;
import com.xsh.trueused.inspection.service.InspectionPdfService;
import com.xsh.trueused.inspection.service.InspectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class InspectionController {

    private final InspectionService inspectionService;
    private final InspectionPdfService inspectionPdfService;

    @PostMapping("/platform/receive_package")
    public ApiResponse<InspectionFlowDTO> receivePackage(@RequestParam Long orderId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || !user.getRoleNames().contains("ROLE_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can trigger platform inspection");
        }
        return ApiResponse.success(inspectionService.triggerInspection(orderId));
    }

    @GetMapping("/inspections/{orderId}/flow")
    public ApiResponse<InspectionFlowDTO> getInspectionFlow(@PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.success(inspectionService.getInspectionFlowForUser(orderId, user.getId()));
    }

    @GetMapping("/inspections/orders/{orderId}/report")
    public ApiResponse<InspectionFlowDTO> getOrderLinkedInspectionFlow(@PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.success(inspectionService.getOrderLinkedInspectionFlowForUser(orderId, user.getId()));
    }

    @GetMapping("/inspections/consignment/{consignmentId}/flow")
    public ApiResponse<InspectionFlowDTO> getConsignmentInspectionFlow(@PathVariable Long consignmentId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.success(inspectionService.getInspectionFlowByConsignmentForUser(consignmentId, user.getId()));
    }

    @GetMapping("/inspections/my")
    public ApiResponse<List<InspectionFlowDTO>> getMyInspections(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.success(inspectionService.getMyInspections(user.getId()));
    }

    @GetMapping(value = "/inspections/{id}/pdf")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        log.info("Request received to generate PDF for inspection ID: {}", id);
        try {
            InspectionFlowDTO inspection = inspectionService.getInspectionByIdForUser(id, user.getId());
            log.info("Found inspection data for ID: {}, Product: {}", id, inspection.getProductTitle());

            byte[] pdfBytes = inspectionPdfService.generateInspectionReportPdf(inspection);
            log.info("PDF stream generated successfully for inspection ID: {}, size: {} bytes", id, pdfBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename("inspection-report-" + id + ".pdf")
                    .build());
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate PDF for inspection ID: {}", id, e);
            // Return error message as plain text if something goes wrong, or letting the global handler handle it.
            // Returning a 500 with the error message in body.
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("PDF Generation Error".getBytes());
        }
    }
}
