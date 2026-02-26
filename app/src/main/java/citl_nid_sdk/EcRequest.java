package citl_nid_sdk;

public class EcRequest {
    public String nid;
    public String dob;
    public String nameEnglish;
    public String fullOcrData;

    public EcRequest(String nid, String dob, String nameEnglish, String fullOcrData) {
        this.nid = nid;
        this.dob = dob;
        this.nameEnglish = nameEnglish;
        this.fullOcrData = fullOcrData;
    }
}
