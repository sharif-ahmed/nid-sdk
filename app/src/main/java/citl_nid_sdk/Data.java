package citl_nid_sdk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Data{
    @SerializedName("transactionId")
    @Expose
    private String transactionId;
    @SerializedName("timeStamp")
    @Expose
    private String timeStamp;
    @SerializedName("ocrData")
    @Expose
    private OcrData ocrData;
    @SerializedName("ecValidation")
    @Expose
    private EcValidation ecValidation;
    @SerializedName("faceMatchDetail")
    @Expose
    private FaceMatchDetail faceMatchDetail;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public OcrData getOcrData() {
        return ocrData;
    }

    public void setOcrData(OcrData ocrData) {
        this.ocrData = ocrData;
    }

    public EcValidation getEcValidation() {
        return ecValidation;
    }

    public void setEcValidation(EcValidation ecValidation) {
        this.ecValidation = ecValidation;
    }

    public FaceMatchDetail getFaceMatchDetail() {
        return faceMatchDetail;
    }

    public void setFaceMatchDetail(FaceMatchDetail faceMatchDetail) {
        this.faceMatchDetail = faceMatchDetail;
    }
}
