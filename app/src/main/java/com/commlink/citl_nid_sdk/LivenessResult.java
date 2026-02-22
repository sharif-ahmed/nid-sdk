package com.commlink.citl_nid_sdk;

import com.google.mlkit.vision.face.Face;

import java.util.List;

public class LivenessResult {
    public boolean eyeBlink;
    public boolean headTurn;
    public boolean smile;
    public boolean isLive;
    public List<Face> faces;
    public String failureReason;
}

