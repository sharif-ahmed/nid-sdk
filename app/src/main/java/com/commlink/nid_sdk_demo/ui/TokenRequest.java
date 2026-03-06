package com.commlink.nid_sdk_demo.ui;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenRequest {

    @SerializedName("clientId")
    @Expose
    private String clientId;
    @SerializedName("clientSecret")
    @Expose
    private String clientSecret;

    public String getClientId() {
        //return clientId;
        return "276893700486-actcrpoiu5vsjq792ond2f7dqg8fe5kg.apps.ecipher.co";
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        //return clientSecret;
        return "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";
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public TokenRequest(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
