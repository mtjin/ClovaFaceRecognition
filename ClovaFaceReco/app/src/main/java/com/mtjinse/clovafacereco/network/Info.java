package com.mtjinse.clovafacereco.network;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Info {

    @SerializedName("size")
    @Expose
    private Size size;
    @SerializedName("faceCount")
    @Expose
    private Integer faceCount;

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Integer getFaceCount() {
        return faceCount;
    }

    public void setFaceCount(Integer faceCount) {
        this.faceCount = faceCount;
    }

}
