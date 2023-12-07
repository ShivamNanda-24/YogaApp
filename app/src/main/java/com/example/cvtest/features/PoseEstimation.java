package com.example.cvtest.features;

import static android.opengl.ETC1.getHeight;
import static android.opengl.ETC1.getWidth;

import android.graphics.Canvas;
import android.util.Log;

import com.example.cvtest.OverlayView;
import com.example.cvtest.helpers.PoseLandmarks;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoseEstimation {
    String pose;
    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private static PoseLandmarkerResult results;
    private static List<NormalizedLandmark> leftArmLandmarks;
    private static List<NormalizedLandmark> rightArmLandmarks;
    private static List<NormalizedLandmark> rightLegLandmarks;
    private static List<NormalizedLandmark> leftHandLandmarks;
    private static List<NormalizedLandmark> rightHandLandmarks;
    private static NormalizedLandmark leftKneePoints;
    static float angleDegrees ;
    static boolean wristsAboveNose;

    // get the button pressed and based on that setPose

    public static Map<String, Object> generatePoseLines(String pose, PoseLandmarks poseLandmarks) {
        Map<String, Object> poseLineMap = new HashMap<>();

        // Check if all required landmarks for arms and hands are non-null in one condition
        if (poseLandmarks.leftShoulder != null && poseLandmarks.leftElbow != null &&
                poseLandmarks.leftWrist != null && poseLandmarks.rightShoulder != null &&
                poseLandmarks.rightElbow != null && poseLandmarks.rightWrist != null &&
                poseLandmarks.rightHip != null && poseLandmarks.rightKnee != null &&
                poseLandmarks.rightFootIndex != null && poseLandmarks.leftPinky != null &&
                poseLandmarks.leftIndex != null && poseLandmarks.leftThumb != null &&
                poseLandmarks.rightPinky != null && poseLandmarks.rightIndex != null &&
                poseLandmarks.rightThumb != null && poseLandmarks.leftKnee != null &&
                poseLandmarks.rightHeel != null && poseLandmarks.leftHeel != null) {

            leftArmLandmarks = Arrays.asList(poseLandmarks.leftShoulder, poseLandmarks.leftElbow, poseLandmarks.leftWrist);
            rightArmLandmarks = Arrays.asList(poseLandmarks.rightShoulder, poseLandmarks.rightElbow, poseLandmarks.rightWrist);
            rightLegLandmarks = Arrays.asList(poseLandmarks.rightHip, poseLandmarks.rightKnee, poseLandmarks.rightHeel);
            leftHandLandmarks = Arrays.asList(poseLandmarks.leftPinky, poseLandmarks.leftIndex, poseLandmarks.leftThumb);
            rightHandLandmarks = Arrays.asList(poseLandmarks.rightPinky, poseLandmarks.rightIndex, poseLandmarks.rightThumb);
            leftKneePoints  = poseLandmarks.leftKnee;

            // Adding each landmark list to the resultMap with descriptive keys
            poseLineMap.put("leftArmLandmarks", leftArmLandmarks);
            poseLineMap.put("rightArmLandmarks", rightArmLandmarks);
            poseLineMap.put("rightLegLandmarks", rightLegLandmarks);
            poseLineMap.put("leftHandLandmarks", leftHandLandmarks);
            poseLineMap.put("rightHandLandmarks", rightHandLandmarks);
            poseLineMap.put("leftKneePoints", leftKneePoints);

        }
        return poseLineMap;
    }

    public static Map<String, Object> computeYogaPose(PoseLandmarks poseLandmarks) {
            Map<String, Object> yogaPoseCalculations = new HashMap<>();

            float distPalms = calculateDistance(poseLandmarks.rightWrist, poseLandmarks.leftWrist);
            float distLeftKneeAndRightFoot = calculateDistance(poseLandmarks.leftKnee, poseLandmarks.rightHeel);
            float angleRightArm = calculateAngle(rightArmLandmarks);
            float angleLeftArm = calculateAngle(leftArmLandmarks);
            float angleRightLeg = calculateAngle(rightLegLandmarks);

            if (poseLandmarks.rightWrist != null && poseLandmarks.leftWrist != null  && poseLandmarks.nose  != null) {
                wristsAboveNose = poseLandmarks.rightWrist.y() < poseLandmarks.nose.y() && poseLandmarks.leftWrist.y() < poseLandmarks.nose.y();
            }
            boolean armsPosition = checkArmsPosition(distPalms);
            boolean armsStretched = checkArmsStretched( angleRightArm,angleLeftArm,wristsAboveNose);
            boolean rightLegAngle = checkRightLegAngle(angleRightLeg);
            boolean kneeFootDistance = checkKneeFootDistance( distLeftKneeAndRightFoot);

            yogaPoseCalculations.put("wristsAboveNose", wristsAboveNose);
            yogaPoseCalculations.put("armsPosition", armsPosition);
            yogaPoseCalculations.put("armsStretched", armsStretched);
            yogaPoseCalculations.put("rightLegAngle", rightLegAngle);
            yogaPoseCalculations.put("kneeFootDistance", kneeFootDistance);

            return  yogaPoseCalculations;
    }

    public static float calculateDistance(NormalizedLandmark point1, NormalizedLandmark point2) {
        if (point1 != null && point2 != null) {
            float x1 = point1.x();
            float y1 = point1.y();
            float x2 = point2.x();
            float y2 = point2.y();
            return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        return 0.0F;
    }

    public static float calculateAngle(List<NormalizedLandmark> landmarks) {
        if (landmarks != null) {
            if (landmarks.size() != 3) {
                throw new IllegalArgumentException("Landmarks array must contain exactly 3 elements.");
            }

            // Extract landmarks
            NormalizedLandmark first = landmarks.get(0);
            NormalizedLandmark middle = landmarks.get(1);
            NormalizedLandmark last = landmarks.get(2);

            // Calculate vectors
            float[] vectorA = {middle.x() - first.x(), middle.y() - first.y()};
            float[] vectorB = {last.x() - middle.x(), last.y() - middle.y()};

            // Calculate dot product
            float dotProduct = vectorA[0] * vectorB[0] + vectorA[1] * vectorB[1];

            // Calculate magnitudes
            float magnitudeA = (float) Math.sqrt(vectorA[0] * vectorA[0] + vectorA[1] * vectorA[1]);
            float magnitudeB = (float) Math.sqrt(vectorB[0] * vectorB[0] + vectorB[1] * vectorB[1]);

            // Calculate angle in radians and then in degrees
            float angle = (float) Math.acos(dotProduct / (magnitudeA * magnitudeB));
            angleDegrees = (float) Math.toDegrees(angle);
        }
        return angleDegrees;
    }

    private static boolean checkArmsPosition(Float distPalms) {
        if (distPalms < 0.1) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean checkArmsStretched(Float angleRightArm, Float angleLeftArm, Boolean wristsAboveNose) {
        if (angleRightArm < 90 && angleLeftArm < 90 && wristsAboveNose) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean checkKneeFootDistance(Float distLeftKneeAndRightFoot) {
        if (distLeftKneeAndRightFoot < 0.1) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean checkRightLegAngle(Float angleRightLeg) {
        if (angleRightLeg > 130) {
            return true;
        } else {

            return false;
        }
    }
}



