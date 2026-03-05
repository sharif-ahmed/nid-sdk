package com.commlink.citl_nid_sdk.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "nid_info")
public class NidInfoEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nidNumber;
    private String fullName;
    private String nameBangla;
    private String dateOfBirth;
    private String fatherName;
    private String fatherNameBangla;
    private String motherName;
    private String motherNameBangla;
    private String addressBangla;
    private String frontImagePath;
    private String backImagePath;
    private long createdAt;

    private String ocrRawDataFront;
    private String ocrRawDataBack;
    private boolean isFaceMatchRequired;

    public boolean isFaceMatchRequired() {
        return isFaceMatchRequired;
    }

    public void setFaceMatchRequired(boolean faceMatchRequired) {
        isFaceMatchRequired = faceMatchRequired;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPhotoBase64St() {
        return photoBase64St;
    }

    public void setPhotoBase64St(String photoBase64St) {
        this.photoBase64St = photoBase64St;
    }

    private String transactionId;
    private String photoBase64St;

    public String getOcrRawDataFront() {
        return ocrRawDataFront;
    }

    public void setOcrRawDataFront(String ocrRawDataFront) {
        this.ocrRawDataFront = ocrRawDataFront;
    }

    public String getOcrRawDataBack() {
        return ocrRawDataBack;
    }

    public void setOcrRawDataBack(String ocrRawDataBack) {
        this.ocrRawDataBack = ocrRawDataBack;
    }

    public NidInfoEntity() {
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public int getId() { return id; }
    public String getNidNumber() { return nidNumber; }
    public String getFullName() { return fullName; }
    public String getNameBangla() { return nameBangla; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getFatherName() { return fatherName; }
    public String getFatherNameBangla() { return fatherNameBangla; }
    public String getMotherName() { return motherName; }
    public String getMotherNameBangla() { return motherNameBangla; }
    public String getAddressBangla() { return addressBangla; }
    public String getFrontImagePath() { return frontImagePath; }
    public String getBackImagePath() { return backImagePath; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNidNumber(String nidNumber) { this.nidNumber = nidNumber; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setNameBangla(String nameBangla) { this.nameBangla = nameBangla; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public void setFatherNameBangla(String fatherNameBangla) { this.fatherNameBangla = fatherNameBangla; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public void setMotherNameBangla(String motherNameBangla) { this.motherNameBangla = motherNameBangla; }
    public void setAddressBangla(String addressBangla) { this.addressBangla = addressBangla; }
    public void setFrontImagePath(String frontImagePath) { this.frontImagePath = frontImagePath; }
    public void setBackImagePath(String backImagePath) { this.backImagePath = backImagePath; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }


}
