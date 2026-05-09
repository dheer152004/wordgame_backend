package com.example.WordGame.DTO;

public class APIResponse {
    private String message;
    private boolean success;
    private Integer statusCode;

    // No-arg constructor (REQUIRED for Spring)
    public APIResponse() {
    }

    public APIResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public APIResponse(String message, boolean success, Integer statusCode) {
        this.message = message;
        this.success = success;
        this.statusCode = statusCode;
    }

    // Getters and Setters (REQUIRED for Spring/Swagger)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}