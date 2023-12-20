package com.example.cvtest.features;

import com.example.cvtest.helpers.PoseLandmarks;
import com.example.cvtest.helpers.TreePoseHelper;
import com.example.cvtest.helpers.WarriorPoseHelper;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoseEstimation {
    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private static List<NormalizedLandmark> leftArmLandmarks;
    private static List<NormalizedLandmark> rightArmLandmarks;
    private static List<NormalizedLandmark> rightLegLandmarks;
    private static List<NormalizedLandmark> leftHandLandmarks;
    private static List<NormalizedLandmark> leftLegLandmarks;
    private static List<NormalizedLandmark> rightHandLandmarks;
    private static NormalizedLandmark leftKneePoints;
    static float angleDegrees;
    static boolean wristsAboveNose;
    private static float distanceLeftRightFeet;

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
                poseLandmarks.rightHeel != null && poseLandmarks.leftHeel != null &&
                poseLandmarks.leftHip != null) {

            leftArmLandmarks = Arrays.asList(poseLandmarks.leftShoulder, poseLandmarks.leftElbow, poseLandmarks.leftWrist);
            rightArmLandmarks = Arrays.asList(poseLandmarks.rightShoulder, poseLandmarks.rightElbow, poseLandmarks.rightWrist);
            rightLegLandmarks = Arrays.asList(poseLandmarks.rightHip, poseLandmarks.rightKnee, poseLandmarks.rightHeel);
            leftLegLandmarks = Arrays.asList(poseLandmarks.leftHip, poseLandmarks.leftKnee, poseLandmarks.leftHeel);
            leftHandLandmarks = Arrays.asList(poseLandmarks.leftPinky, poseLandmarks.leftIndex, poseLandmarks.leftThumb);
            rightHandLandmarks = Arrays.asList(poseLandmarks.rightPinky, poseLandmarks.rightIndex, poseLandmarks.rightThumb);
            leftKneePoints = poseLandmarks.leftKnee;
            if (pose.equals("Tree Pose")) {

                // Adding each landmark list to the resultMap with descriptive keys
                poseLineMap.put("leftArmLandmarks", leftArmLandmarks);
                poseLineMap.put("rightArmLandmarks", rightArmLandmarks);
                poseLineMap.put("rightLegLandmarks", rightLegLandmarks);
                poseLineMap.put("leftHandLandmarks", leftHandLandmarks);
                poseLineMap.put("rightHandLandmarks", rightHandLandmarks);
                poseLineMap.put("leftKneePoints", leftKneePoints);
            } else if (pose.equals("Warrior Pose") || pose.equals("Routine")) {

                // Adding each landmark list to the resultMap with descriptive keys
                poseLineMap.put("leftArmLandmarks", leftArmLandmarks);
                poseLineMap.put("rightArmLandmarks", rightArmLandmarks);
                poseLineMap.put("rightLegLandmarks", rightLegLandmarks);
                poseLineMap.put("leftLegLandmarks", leftLegLandmarks);
            }

        }
        return poseLineMap;
    }

    public static Map<String, Object> yogaPoseCalculations(String pose, PoseLandmarks poseLandmarks) {
        Map<String, Object> angles = new HashMap<>();

        float angleRightLeg = calculateAngle(rightLegLandmarks);
        float angleLeftLeg = calculateAngle(leftLegLandmarks);
        float distanceBetweenLegs = distanceLeftRightFeet;

        angles.put("angleRightLeg", angleRightLeg);
        angles.put("distanceBetweenLegs", distanceBetweenLegs);
        angles.put("angleLeftLeg", angleLeftLeg);

        return angles;
    }

    public static Map<String, Object> computeYogaPose(String pose, PoseLandmarks poseLandmarks) {
        Map<String, Object> yogaPoseCalculations = new HashMap<>();
        float distPalms = calculateDistance(poseLandmarks.rightWrist, poseLandmarks.leftWrist);
        float distLeftKneeAndRightFoot = calculateDistance(poseLandmarks.leftKnee, poseLandmarks.rightHeel);
        distanceLeftRightFeet = calculateDistance(poseLandmarks.leftHeel, poseLandmarks.rightHeel);

        float angleRightArm = calculateAngle(rightArmLandmarks);
        float angleLeftArm = calculateAngle(leftArmLandmarks);
        float angleRightLeg = calculateAngle(rightLegLandmarks);
        float angleLeftLeg = calculateAngle(leftLegLandmarks);


        if (pose.equals("Tree Pose")) {
            if (poseLandmarks.rightWrist != null && poseLandmarks.leftWrist != null && poseLandmarks.nose != null) {
                wristsAboveNose = poseLandmarks.rightWrist.y() < poseLandmarks.nose.y() && poseLandmarks.leftWrist.y() < poseLandmarks.nose.y();
            }
            boolean armsPosition = TreePoseHelper.checkArmsPosition(distPalms);
            boolean armsStretched = TreePoseHelper.checkArmsStretched(angleRightArm, angleLeftArm, wristsAboveNose);
            boolean rightLegAngle = TreePoseHelper.checkRightLegAngle(angleRightLeg);
            boolean kneeFootDistance = TreePoseHelper.checkKneeFootDistance(distLeftKneeAndRightFoot);

            yogaPoseCalculations.put("wristsAboveNose", wristsAboveNose);
            yogaPoseCalculations.put("armsPosition", armsPosition);
            yogaPoseCalculations.put("armsStretched", armsStretched);
            yogaPoseCalculations.put("rightLegAngle", rightLegAngle);
            yogaPoseCalculations.put("kneeFootDistance", kneeFootDistance);
        } else if (pose.equals("Warrior Pose") || pose.equals("Routine")) {

            boolean rightArmStretched = WarriorPoseHelper.checkArmsStretchedOut(angleRightArm);
            boolean leftArmStretched = WarriorPoseHelper.checkArmsStretchedOut(angleLeftArm);
            boolean feetDistance = WarriorPoseHelper.checkDistance(distanceLeftRightFeet, 0.3);
            boolean leftLegAngle = WarriorPoseHelper.checkLeftLegAngle(angleLeftLeg, 15F);
            boolean rightLegAngle = WarriorPoseHelper.checkRightLegAngle(angleRightLeg, 70F);

            yogaPoseCalculations.put("rightArmStretched", rightArmStretched);
            yogaPoseCalculations.put("leftArmStretched", leftArmStretched);
            yogaPoseCalculations.put("feetDistance", feetDistance);
            yogaPoseCalculations.put("leftLegAngle", leftLegAngle);
            yogaPoseCalculations.put("rightLegAngle", rightLegAngle);
        }
        return yogaPoseCalculations;
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
}


