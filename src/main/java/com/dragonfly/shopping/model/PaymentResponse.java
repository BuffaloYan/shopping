package com.dragonfly.shopping.model;

public class PaymentResponse {
    private String status;
    private String requestId;
    private String invoiceId;
    private String errorMessage;

    // Default constructor for Jackson
    public PaymentResponse() {
    }

    public PaymentResponse(String status, String requestId, String invoiceId, String errorMessage) {
        this.status = status;
        this.requestId = requestId;
        this.invoiceId = invoiceId;
        this.errorMessage = errorMessage;
    }

    public String status() {
        return status;
    }

    public String requestId() {
        return requestId;
    }

    public String invoiceId() {
        return invoiceId;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
