package com.example.ForgeHubs.exception;

import com.example.ForgeHubs.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request, Model model) {
        log.warn("Resource not found | method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request, model);
    }

    @ExceptionHandler(com.example.ForgeHubs.Exception.BusinessException.class)
    public Object handleBusiness(com.example.ForgeHubs.Exception.BusinessException ex, HttpServletRequest request, Model model) {
        log.warn("Business exception | method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request, model);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request, Model model) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(". "));
        if (message.isBlank()) message = "Please check the entered information.";
        log.warn("Validation exception | method={} path={} message={}", request.getMethod(), request.getRequestURI(), message);
        return buildResponse(message, HttpStatus.BAD_REQUEST, request, model);
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unexpected application error | method={} path={}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse("Something went wrong while processing your request. Please try again.",
                HttpStatus.INTERNAL_SERVER_ERROR, request, model);
    }

    private Object buildResponse(String message, HttpStatus status, HttpServletRequest request, Model model) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(status).body(new com.example.ForgeHubs.Exception.ErrorResponse(
                    LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
        }
        model.addAttribute("status", status.value());
        model.addAttribute("error", status.getReasonPhrase());
        model.addAttribute("message", message);
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return request.getRequestURI().startsWith("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }
}
