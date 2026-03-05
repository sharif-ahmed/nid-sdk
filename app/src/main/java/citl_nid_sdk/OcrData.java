package citl_nid_sdk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OcrData implements Serializable {
    @SerializedName("nameEnglish")
    @Expose
    private String nameEnglish;
    @SerializedName("nameBangla")
    @Expose
    private String nameBangla;
    @SerializedName("fatherName")
    @Expose
    private String fatherName;
    @SerializedName("motherName")
    @Expose
    private String motherName;
    @SerializedName("nidNumber")
    @Expose
    private String nidNumber;
    @SerializedName("dateOfBirth")
    @Expose
    private String dateOfBirth;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("isFaceMatchRequired")
    @Expose
    private Boolean isFaceMatchRequired;
    @SerializedName("photoBase64St")
    @Expose
    private Object photoBase64St;
    @SerializedName("transactionid")
    @Expose
    private String transactionid;

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

    public String getNidNumber() {
        return nidNumber;
    }

    public void setNidNumber(String nidNumber) {
        this.nidNumber = nidNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public Object getPhotoBase64St() {
        return photoBase64St;
    }

    public void setPhotoBase64St(Object photoBase64St) {
        this.photoBase64St = photoBase64St;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }
}
