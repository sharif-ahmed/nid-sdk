package citl_nid_sdk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EcValidation implements Serializable {

    @SerializedName("nidMatched")
    @Expose
    private Boolean nidMatched;
    @SerializedName("dobMatched")
    @Expose
    private Boolean dobMatched;
    @SerializedName("nameMatched")
    @Expose
    private Boolean nameMatched;

    public Boolean getNidMatched() {
        return nidMatched;
    }

    public void setNidMatched(Boolean nidMatched) {
        this.nidMatched = nidMatched;
    }

    public Boolean getDobMatched() {
        return dobMatched;
    }

    public void setDobMatched(Boolean dobMatched) {
        this.dobMatched = dobMatched;
    }

    public Boolean getNameMatched() {
        return nameMatched;
    }

    public void setNameMatched(Boolean nameMatched) {
        this.nameMatched = nameMatched;
    }
}
