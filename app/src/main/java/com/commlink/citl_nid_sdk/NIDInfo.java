package com.commlink.citl_nid_sdk;

import android.graphics.Bitmap;

import java.io.Serializable;

public class NIDInfo implements Serializable {
    private String nidNumber;
    private String name;
    private String dateOfBirth;
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

