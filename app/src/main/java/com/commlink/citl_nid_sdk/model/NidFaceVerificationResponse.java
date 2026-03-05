package com.commlink.citl_nid_sdk.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NidFaceVerificationResponse {
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("result")
    @Expose
    public Result result;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
