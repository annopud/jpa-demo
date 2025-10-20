package dev.annopud.jpa_demo.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EmailStatus {
    SUCCESS("S", "Success"),
    ERROR("E", "Failure");

    private final String code;
    private final String message;

    EmailStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static EmailStatus fromCode(String code) {
        for (EmailStatus value : EmailStatus.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("EmailStatus not found [" + code + "]");
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

