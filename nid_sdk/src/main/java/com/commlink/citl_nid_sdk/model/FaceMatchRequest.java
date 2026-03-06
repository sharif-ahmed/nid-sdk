package com.commlink.citl_nid_sdk.model;


public class FaceMatchRequest {
    public String nid;
    public String dob;
    public String nameEnglish;
    public String selfie_base64;

    public FaceMatchRequest(String nid, String dob, String nameEnglish, String selfie_base64) {
        this.nid = nid;
        this.dob = dob;
        this.nameEnglish = nameEnglish;
        this.selfie_base64 = selfie_base64;
    }
}
