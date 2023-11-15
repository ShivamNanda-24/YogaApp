package com.example.cvtest;

import androidx.lifecycle.ViewModel;

/**
 * This ViewModel is used to store pose landmarker helper settings.
 */
public class MainViewModel extends ViewModel {
    private int _model = PoseLandmarkerHelper.MODEL_POSE_LANDMARKER_FULL;
    private int _delegate = PoseLandmarkerHelper.DELEGATE_CPU;
    private float _minPoseDetectionConfidence = PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE;
    private float _minPoseTrackingConfidence = PoseLandmarkerHelper.DEFAULT_POSE_TRACKING_CONFIDENCE;
    private float _minPosePresenceConfidence = PoseLandmarkerHelper.DEFAULT_POSE_PRESENCE_CONFIDENCE;

    public int getCurrentDelegate() {
        return _delegate;
    }

    public int getCurrentModel() {
        return _model;
    }

    public float getCurrentMinPoseDetectionConfidence() {
        return _minPoseDetectionConfidence;
    }

    public float getCurrentMinPoseTrackingConfidence() {
        return _minPoseTrackingConfidence;
    }

    public float getCurrentMinPosePresenceConfidence() {
        return _minPosePresenceConfidence;
    }

    public void setDelegate(int delegate) {
        _delegate = delegate;
    }

    public void setMinPoseDetectionConfidence(float confidence) {
        _minPoseDetectionConfidence = confidence;
    }

    public void setMinPoseTrackingConfidence(float confidence) {
        _minPoseTrackingConfidence = confidence;
    }

    public void setMinPosePresenceConfidence(float confidence) {
        _minPosePresenceConfidence = confidence;
    }

    public void setModel(int model) {
        _model = model;
    }
}