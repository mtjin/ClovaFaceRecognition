package com.mtjinse.clovafacereco.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Celebrity {

    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("confidence")
    @Expose
    private Double confidence;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
