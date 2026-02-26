package citl_nid_sdk;

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

    // New error codes as specified by user
    public static final String E100 = "E100"; // OCR Failed
    public static final String E101 = "E101"; // NID / DOB Mismatch
    public static final String E102 = "E102"; // EC API Timeout
    public static final String E103 = "E103"; // Liveliness Failed
    public static final String E104 = "E104"; // Face Match Failed
    public static final String E105 = "E105"; // Face Match API Timeout
    public static final String E500 = "E500"; // Unexpected SDK Error

    private final Code code;
    private final String customErrorCode;
    private final String message;
    private final Throwable cause;

    public NIDError(Code code, String message) {
        this(code, message, null, null);
    }

    public NIDError(Code code, String message, String customErrorCode) {
        this(code, message, customErrorCode, null);
    }

    public NIDError(Code code, String message, Throwable cause) {
        this(code, message, null, cause);
    }

    public NIDError(Code code, String message, String customErrorCode, Throwable cause) {
        this.code = code;
        this.message = message;
        this.customErrorCode = customErrorCode;
        this.cause = cause;
    }

    public Code getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getCustomErrorCode() {
        return customErrorCode;
    }

    public Throwable getCause() {
        return cause;
    }
}

