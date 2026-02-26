package citl_nid_sdk;

public class SdkResponse {
    public String status;
    public String transactionId;
    public String message;
    public String errorCode;
    public String timeStamp;
    public OcrData ocrData;
    public EcValidation ecValidation;
    public Boolean faceMatch;

    public static class OcrData {
        public String nameEnglish;
        public String nameBangla;
        public String fatherName;
        public String motherName;
        public String nidNumber;
        public String dateOfBirth; // yyyy-mm-dd
        public String address;
    }

    public static class EcValidation {
        public boolean nidMatch;
        public boolean dobMatch;
        public boolean nameMatch;
    }
}
