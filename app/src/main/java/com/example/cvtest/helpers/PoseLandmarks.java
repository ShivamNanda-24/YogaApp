package com.example.cvtest.helpers;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.List;

public class PoseLandmarks {
    // Fields for each landmark
    public NormalizedLandmark nose;
    public NormalizedLandmark leftEyeInner;
    public NormalizedLandmark leftEye;
    public NormalizedLandmark leftEyeOuter;
    public NormalizedLandmark rightEyeInner;
    public NormalizedLandmark rightEye;
    public NormalizedLandmark rightEyeOuter;
    public NormalizedLandmark leftEar;
    public NormalizedLandmark rightEar;
    public NormalizedLandmark mouthLeft;
    public NormalizedLandmark mouthRight;
    public NormalizedLandmark leftShoulder;
    public NormalizedLandmark rightShoulder;
    public NormalizedLandmark leftElbow;
    public NormalizedLandmark rightElbow;
    public NormalizedLandmark leftWrist;
    public NormalizedLandmark rightWrist;
    public NormalizedLandmark leftPinky;
    public NormalizedLandmark rightPinky;
    public NormalizedLandmark leftIndex;
    public NormalizedLandmark rightIndex;
    public NormalizedLandmark leftThumb;
    public NormalizedLandmark rightThumb;
    public NormalizedLandmark leftHip;
    public NormalizedLandmark rightHip;
    public NormalizedLandmark leftKnee;
    public NormalizedLandmark rightKnee;
    public NormalizedLandmark leftAnkle;
    public NormalizedLandmark rightAnkle;
    public NormalizedLandmark leftHeel;
    public NormalizedLandmark rightHeel;
    public NormalizedLandmark leftFootIndex;
    public NormalizedLandmark rightFootIndex;
    private PoseLandmarkerResult results;
    public List<NormalizedLandmark> landmarks;

    // Constructor
    public PoseLandmarks(PoseLandmarkerResult results) {
        this.results = results;
        initializeLandmarks();
    }

    private void initializeLandmarks() {
        if (results != null && !results.landmarks().isEmpty()) {
            landmarks = results.landmarks().get(0);

            // Initialize landmarks
            nose = getLandmark(landmarks, 0);
            leftEyeInner = getLandmark(landmarks, 2);
            leftEye = getLandmark(landmarks, 4);
            leftEyeOuter = getLandmark(landmarks, 6);
            rightEyeInner = getLandmark(landmarks, 1);
            rightEye = getLandmark(landmarks, 3);
            rightEyeOuter = getLandmark(landmarks, 5);
            leftEar = getLandmark(landmarks, 8);
            rightEar = getLandmark(landmarks, 7);
            mouthLeft = getLandmark(landmarks, 10);
            mouthRight = getLandmark(landmarks, 9);
            leftShoulder = getLandmark(landmarks, 12);
            rightShoulder = getLandmark(landmarks, 11);
            leftElbow = getLandmark(landmarks, 14);
            rightElbow = getLandmark(landmarks, 13);
            leftWrist = getLandmark(landmarks, 16);
            rightWrist = getLandmark(landmarks, 15);
            leftPinky = getLandmark(landmarks, 18);
            rightPinky = getLandmark(landmarks, 17);
            leftIndex = getLandmark(landmarks, 20);
            rightIndex = getLandmark(landmarks, 19);
            leftThumb = getLandmark(landmarks, 22);
            rightThumb = getLandmark(landmarks, 21);
            leftHip = getLandmark(landmarks, 24);
            rightHip = getLandmark(landmarks, 23);
            leftKnee = getLandmark(landmarks, 26);
            rightKnee = getLandmark(landmarks, 25);
            leftAnkle = getLandmark(landmarks, 28);
            rightAnkle = getLandmark(landmarks, 27);
            leftHeel = getLandmark(landmarks, 30);
            rightHeel = getLandmark(landmarks, 29);
            leftFootIndex = getLandmark(landmarks, 32);
            rightFootIndex = getLandmark(landmarks, 31);
        }
    }

    private NormalizedLandmark getLandmark(List<NormalizedLandmark> landmarks, int index) {
        return index < landmarks.size() ? landmarks.get(index) : null;
    }

    // Additional methods for any other specific landmark processing can be added here
}
