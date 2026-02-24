package citl_nid_sdk;

public class NidVerifyRequest {
    public String nid;
    public String dob;
    public String selfie_base64;
    public String front_base64;
    public String back_base64;
    public String device_id;
    public String encrypted;

    public NidVerifyRequest(String nid, String dob, String selfie_base64, String front_base64, String back_base64, String device_id) {
        this.nid = nid;
        this.dob = dob;
        this.selfie_base64 = selfie_base64;
        this.front_base64 = front_base64;
        this.back_base64 = back_base64;
        this.device_id = device_id;
    }
}

