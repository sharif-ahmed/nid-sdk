package com.commlink.citl_nid_sdk;

public class NIDError {

    public enum Code {
        LICENSE_INVALID,
        CAMERA_ERROR,
        OCR_ERROR,
        LIVENESS_FAILED,
        FACE_MATCH_FAILED,
        NETWORK_ERROR,
        UNKNOWN
    }

    private final Code code;
    private final String message;
    private final Throwable cause;

    public NIDError(Code code, String message) {
        this(code, message, null);
    }

    public NIDError(Code code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        this.cause = cause;
    }

    public Code getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}

