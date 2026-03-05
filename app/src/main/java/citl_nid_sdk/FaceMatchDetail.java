package citl_nid_sdk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FaceMatchDetail implements Serializable {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("faceMatchScore")
    @Expose
    private double faceMatchScore;
    @SerializedName("faceMatchCode")
    @Expose
    private String faceMatchCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getFaceMatchScore() {
        return faceMatchScore;
    }

    public void setFaceMatchScore(double faceMatchScore) {
        this.faceMatchScore = faceMatchScore;
    }

    public String getFaceMatchCode() {
        return faceMatchCode;
    }

    public void setFaceMatchCode(String faceMatchCode) {
        this.faceMatchCode = faceMatchCode;
    }
}
