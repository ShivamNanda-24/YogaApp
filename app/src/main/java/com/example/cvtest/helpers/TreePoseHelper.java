package com.example.cvtest.helpers;

public class TreePoseHelper {
    public static boolean checkArmsPosition(Float distPalms) {
        if (distPalms < 0.1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkArmsStretched(Float angleRightArm, Float angleLeftArm, Boolean wristsAboveNose) {
        if (angleRightArm < 90 && angleLeftArm < 90 && wristsAboveNose) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkKneeFootDistance(Float distLeftKneeAndRightFoot) {
        if (distLeftKneeAndRightFoot < 0.1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkRightLegAngle(Float angleRightLeg) {
        if (angleRightLeg > 130) {
            return true;
        } else {
            return false;
        }
    }
}

