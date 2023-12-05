package com.example.cvtest.features;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.List;

public class PoseEstimation {


    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private PoseLandmarkerResult results;

    // get the button pressed and based on that setPose


    public static void setResults(
            PoseLandmarkerResult poseLandmarkerResults,
            int imageHeight,
            int imageWidth
    ) {

    }
    public static float calculateDistance(NormalizedLandmark point1, NormalizedLandmark point2) {
        float x1 = point1.x();
        float y1 = point1.y();
        float x2 = point2.x();
        float y2 = point2.y();

        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static float calculateAngle(List<NormalizedLandmark> landmarks) {
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
        float angleDegrees = (float) Math.toDegrees(angle);

        return angleDegrees;
    }
}
