package citl_nid_sdk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NidECVerifyRequest {

    @SerializedName("nidNumber")
    @Expose
    private String nidNumber;
    @SerializedName("nameEnglish")
    @Expose
    private String nameEnglish;
    @SerializedName("nameBangla")
    @Expose
    private String nameBangla;
    @SerializedName("dateOfBirth")
    @Expose
    private String dateOfBirth;
    @SerializedName("fatherName")
    @Expose
    private String fatherName;
    @SerializedName("motherName")
    @Expose
    private String motherName;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("isFaceMatchRequired")
    @Expose
    private Boolean isFaceMatchRequired;
    @SerializedName("transactionid")
    @Expose
    private String transactionid;
    @SerializedName("photoBase64St")
    @Expose
    private String photoBase64St;

    public String getNidNumber() {
        return nidNumber;
    }

    public void setNidNumber(String nidNumber) {
        this.nidNumber = nidNumber;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getNameBangla() {
        return nameBangla;
    }

    public void setNameBangla(String nameBangla) {
        this.nameBangla = nameBangla;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getIsFaceMatchRequired() {
        return isFaceMatchRequired;
    }

    public void setIsFaceMatchRequired(Boolean isFaceMatchRequired) {
        this.isFaceMatchRequired = isFaceMatchRequired;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    public String getPhotoBase64St() {
        return photoBase64St;
    }

    public void setPhotoBase64St(String photoBase64St) {
        this.photoBase64St = photoBase64St;
    }

    public NidECVerifyRequest(String nidNumber, String nameEnglish, String nameBangla, String dateOfBirth, String fatherName, String motherName, String address, Boolean isFaceMatchRequired) {
        this.nidNumber = nidNumber;
        this.nameEnglish = nameEnglish;
        this.nameBangla = nameBangla;
        this.dateOfBirth = dateOfBirth;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.address = address;
        this.isFaceMatchRequired = isFaceMatchRequired;
    }
}
