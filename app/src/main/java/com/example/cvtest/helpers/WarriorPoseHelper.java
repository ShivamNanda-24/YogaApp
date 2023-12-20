package com.example.cvtest.helpers;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

public class WarriorPoseHelper {
    PoseLandmarks poseLandmarks;
    public WarriorPoseHelper(PoseLandmarks poseLandmarks){
        this.poseLandmarks = poseLandmarks;
    }
    public static boolean checkArmsStretchedOut (Float angleArm) {
        if (angleArm < 18) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkDistance(Float distance, Double minLen) {
        if (distance > minLen) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkRightLegAngle(Float angleLeg, Float minAngle) {
        if (angleLeg > minAngle) {
            return true;
        } else {

            return false;
        }
    }

    public static boolean checkLeftLegAngle(Float angleLeg, Float minAngle) {
        if (angleLeg < minAngle) {
            return true;
        } else {

            return false;
        }
    }
}
