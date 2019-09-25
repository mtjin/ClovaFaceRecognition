package com.mtjinse.clovafacereco.network;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NaverResult {

    @SerializedName("info")
    @Expose
    private Info info;
    @SerializedName("faces")
    @Expose
    private List<Face> faces = null;

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public void setFaces(List<Face> faces) {
        this.faces = faces;
    }

}