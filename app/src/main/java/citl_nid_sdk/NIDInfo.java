package citl_nid_sdk;

import android.graphics.Bitmap;

import java.io.Serializable;

public class NIDInfo implements Serializable {
    private String nidNumber;
    private String name;
    private String nameBangla;
    private String dateOfBirth;
    private String fatherName;
    private String fatherNameBangla;
    private String motherName;
    private String motherNameBangla;
    private String addressBangla;
    private String ocrRawData;

    public String getOcrRawData() {
        return ocrRawData;
    }

    public void setOcrRawData(String ocrRawData) {
        this.ocrRawData = ocrRawData;
    }

    private transient Bitmap nidFrontImage;
    private transient Bitmap selfieImage;

    public NIDInfo(String nidNumber, String name, String dateOfBirth) {
        this.nidNumber = nidNumber;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    public String getNidNumber() {
        return nidNumber;
    }

    public void setNidNumber(String nidNumber) {
        this.nidNumber = nidNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNameBangla() {
        return nameBangla;
    }

    public void setNameBangla(String nameBangla) {
        this.nameBangla = nameBangla;
    }

    public String getFatherNameBangla() {
        return fatherNameBangla;
    }

    public void setFatherNameBangla(String fatherNameBangla) {
        this.fatherNameBangla = fatherNameBangla;
    }

    public String getMotherNameBangla() {
        return motherNameBangla;
    }

    public void setMotherNameBangla(String motherNameBangla) {
        this.motherNameBangla = motherNameBangla;
    }

    public String getAddressBangla() {
        return addressBangla;
    }

    public void setAddressBangla(String addressBangla) {
        this.addressBangla = addressBangla;
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

    public Bitmap getNidFrontImage() {
        return nidFrontImage;
    }

    public void setNidFrontImage(Bitmap nidFrontImage) {
        this.nidFrontImage = nidFrontImage;
    }

    public Bitmap getSelfieImage() {
        return selfieImage;
    }

    public void setSelfieImage(Bitmap selfieImage) {
        this.selfieImage = selfieImage;
    }
}

