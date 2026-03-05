package citl_nid_sdk;

public class NidFaceVerificationRequest {
    public String nidNumber;
    public String nameEnglish;
    public String dateOfBirth; // dd-MM-yyyy
    public boolean isFaceMatchRequired;
    public String transactionid; // UUID as String
    public String photoBase64St;

    public NidFaceVerificationRequest(String nidNumber, String nameEnglish, String dateOfBirth, 
                                    boolean isFaceMatchRequired, String transactionid, String photoBase64St) {
        this.nidNumber = nidNumber;
        this.nameEnglish = nameEnglish;
        this.dateOfBirth = dateOfBirth;
        this.isFaceMatchRequired = isFaceMatchRequired;
        this.transactionid = transactionid;
        this.photoBase64St = photoBase64St;
    }
}
