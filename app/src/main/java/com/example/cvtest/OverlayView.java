package com.example.cvtest;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cvtest.features.CustomTimer;
import com.example.cvtest.features.PoseEstimation;
import com.example.cvtest.helpers.PoseLandmarks;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OverlayView extends View {
    private static final float LANDMARK_STROKE_WIDTH = 35F;
    private PoseLandmarkerResult results;
    private final Paint incorrectPointPaint = new Paint();
    private final Paint correctPointPaint = new Paint();
    private final Paint incorrectPaint = new Paint();
    private final Paint correctPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint backgroundColor = new Paint();
    private final Paint warningTextPaint = new Paint();
    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private CustomTimer customTimer;
    private static final MutableLiveData<String> message = new MutableLiveData<>();
    private static final MutableLiveData<String> timer = new MutableLiveData<>();
    private PoseLandmarks poseLandmarks;
    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
        initCustomTimer();
    }

    public void clear() {
        results = null;
        incorrectPointPaint.reset();
        incorrectPaint.reset();
        backgroundColor.reset();
        invalidate();
        initPaints();
    }

    private void initPaints() {
        incorrectPaint.setColor(Color.RED);
        incorrectPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        incorrectPaint.setStyle(Paint.Style.STROKE);

        correctPaint.setColor(Color.GREEN);
        correctPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        correctPaint.setStyle(Paint.Style.STROKE);

        incorrectPointPaint.setColor(Color.RED);
        incorrectPointPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        incorrectPointPaint.setStyle(Paint.Style.FILL);

        correctPointPaint.setColor(Color.GREEN);
        correctPointPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        correctPointPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.RED); // Set the text color
        textPaint.setTextSize(90);

        backgroundColor.setColor(Color.argb(200, 0, 0, 0));

        warningTextPaint.setColor(Color.WHITE);
        warningTextPaint.setTextSize(100);
        warningTextPaint.setTextAlign(Paint.Align.CENTER);

    }

//    public static void receiveLandmarks(List<List<NormalizedLandmark>> landmarks) {
//        landmarkList = landmarks;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (poseLandmarks != null) {
            if (checkLandmarkVisibility(poseLandmarks)) {


                Map<String, Object> poseAnalysisResult = PoseEstimation.generatePoseLines("Tree Pose", poseLandmarks);
                Map<String, Object> poseAnalysisCalculations = PoseEstimation.computeYogaPose(poseLandmarks);
//
                boolean wristsAboveNose = (boolean) poseAnalysisCalculations.get("wristsAboveNose");
                boolean armsPosition = (boolean) poseAnalysisCalculations.get("armsPosition");
                boolean armsStretched = (boolean) poseAnalysisCalculations.get("armsStretched");
                boolean rightLegAngle = (boolean) poseAnalysisCalculations.get("rightLegAngle");
                boolean kneeFootDistance = (boolean) poseAnalysisCalculations.get("kneeFootDistance");

                List<NormalizedLandmark> leftArmLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("leftArmLandmarks");
                List<NormalizedLandmark> rightArmLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("rightArmLandmarks");
                List<NormalizedLandmark> rightLegLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("rightLegLandmarks");
                List<NormalizedLandmark> leftHandLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("leftHandLandmarks");
                List<NormalizedLandmark> rightHandLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("rightHandLandmarks");
                NormalizedLandmark leftKneePoints = (NormalizedLandmark) poseAnalysisResult.get("leftKneePoints");

                if (wristsAboveNose && armsPosition) {
                    drawLine(canvas, leftHandLandmarks, correctPointPaint);
                    drawLine(canvas, rightHandLandmarks, correctPointPaint);

                } else {
                    drawLine(canvas, leftHandLandmarks, incorrectPaint);
                    drawLine(canvas, rightHandLandmarks, incorrectPaint);
                    setMessage("Put both hands together");

                }

                if (wristsAboveNose && armsStretched) {
                    drawLine(canvas, rightArmLandmarks, correctPointPaint);
                    drawLine(canvas, leftArmLandmarks, correctPointPaint);

                } else {
                    drawLine(canvas, rightArmLandmarks, incorrectPaint);
                    drawLine(canvas, leftArmLandmarks, incorrectPaint);
                    setMessage("Stretch both your hands over your head");

                }

                if (kneeFootDistance) {
                    if (leftKneePoints != null) {
                        canvas.drawPoint(leftKneePoints.x() * imageWidth * scaleFactor, leftKneePoints.y() * imageHeight * scaleFactor, correctPointPaint);
                    }
                } else {
                    if (leftKneePoints != null) {
                        canvas.drawPoint(leftKneePoints.x() * imageWidth * scaleFactor, leftKneePoints.y() * imageHeight * scaleFactor, incorrectPaint);
                        setMessage("Place your right foot closer to your left knee");
                    }
                }

                if (rightLegAngle) {
                    drawLine(canvas, rightLegLandmarks, correctPointPaint);
                } else {
                    drawLine(canvas, rightLegLandmarks, incorrectPaint);
                    setMessage("Stretch right leg outwards");
                }

                if (armsPosition && armsStretched && kneeFootDistance && rightLegAngle) {
                    setMessage("HOLD");
                    // Make a ready set go
                    if (!customTimer.isRunning()) {
                        customTimer.start();
                    }
                } else {
                    if (customTimer.isRunning()) {
                        customTimer.pause();
                        setMessage("Paused");
                    }
                }
            }
            else{
                canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);

                float centerY = getHeight() / 2f;
                float firstLineY = centerY - warningTextPaint.getTextSize();
                float secondLineY = centerY;

                canvas.drawText("Please take a few", getWidth() / 2f, firstLineY, warningTextPaint);
                canvas.drawText("steps back", getWidth() / 2f, secondLineY, warningTextPaint);
            }
//            drawLine(canvas, rightLegLandmarks, correctPointPaint);
//            drawLine(canvas, leftHandLandmarks, correctPointPaint);
//            drawLine(canvas, rightHandLandmarks, correctPointPaint);

            // Loop over the HashMap
//            for (Map.Entry<String, Object> entry : poseAnalysisResult.entrySet()) {
//                String key = entry.getKey();
//                List<NormalizedLandmark> landmarks = (List<NormalizedLandmark>) entry.getValue();
//
//                boolean isAnyLandmarkLessVisible = false;
////                boolean isAnyLandmarkLessVisible = landmarkList.stream()
////                        .anyMatch(landmark -> landmark.visibility().orElse(1.0f) < 0.50f);
//                if (isAnyLandmarkLessVisible) {
//                    canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);
//
//                    float centerY = getHeight() / 2f;
//                    float firstLineY = centerY - warningTextPaint.getTextSize();
//                    float secondLineY = centerY;
//
//                    canvas.drawText("Please take a few", getWidth() / 2f, firstLineY, warningTextPaint);
//                    canvas.drawText("steps back", getWidth() / 2f, secondLineY, warningTextPaint);
//                } else {
//
//                    if ((key.equals("leftHandLandmarks") || key.equals("rightHandLandmarks")) && wristsAboveNose && armsPosition ){
//                        drawLine(canvas, landmarks, correctPointPaint);
//                    }
//                    else{
//                        drawLine(canvas, landmarks, incorrectPaint);
//                    }
//                    if ((key.equals("leftArmLandmarks") || key.equals("rightArmLandmarks")) && armsStretched && wristsAboveNose ){
//                        drawLine(canvas, landmarks, correctPointPaint);
//                    }
//                    else{
//                        drawLine(canvas, landmarks, incorrectPaint);
//                    }
////                    if (key.equals("rightLegLandmarks")  && rightLegAngle && kneeFootDistance ){
////                        drawLine(canvas, landmarks, correctPointPaint);
////                    }
//
////                    if ((key.equals("leftHandLandmarks") || key.equals("rightHandLandmarks")) && wristsAboveNose && armsPosition ){
////                        drawLine(canvas, landmarks, correctPointPaint);
////                    }
//
//                }
//            }
        }
    }

    private boolean checkLandmarkVisibility(PoseLandmarks poseLandmarks) {
        final float VISIBILITY_THRESHOLD = 0.2F; // Example threshold, adjust as needed
        if(poseLandmarks.landmarks != null) {
            for (NormalizedLandmark landmark : poseLandmarks.landmarks) {
                // Check if the landmark's visibility is present and below the threshold
                if (landmark.visibility().isPresent() && landmark.visibility().get() < VISIBILITY_THRESHOLD) {

                    return true; // Less visible landmark found
                }
            }
        }

        return false;
    }




    private void initCustomTimer() {
        customTimer = new CustomTimer(30000, 1000, new CustomTimer.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimer(String.valueOf(millisUntilFinished/1000));
            }
        });
    }


    private void drawLine(Canvas canvas, List<NormalizedLandmark> landmarks, Paint paint) {
        if(landmarks != null) {
            for (int i = 0; i < landmarks.size() - 1; i++) {
                // Get the current and next landmark
                NormalizedLandmark currentLandmark = landmarks.get(i);
                NormalizedLandmark nextLandmark = landmarks.get(i + 1);

                // Calculate screen coordinates for current and next landmarks
                float startX = currentLandmark.x() * imageWidth * scaleFactor;
                float startY = currentLandmark.y() * imageHeight * scaleFactor;
                float endX = nextLandmark.x() * imageWidth * scaleFactor;
                float endY = nextLandmark.y() * imageHeight * scaleFactor;

                // Draw a line between the landmarks
                canvas.drawLine(startX, startY, endX, endY, paint);
            }
        }
    }



    public static LiveData<String> getMessage() {
        return message;
    }

    public static LiveData<String> getTimer() {
        return timer;
    }

    public void setMessage(String newMessage) {
        message.postValue(newMessage);
    }

    public void setTimer(String newTime) {
        timer.postValue(newTime);
    }


    public void setResults(
            PoseLandmarkerResult poseLandmarkerResults,
            int imageHeight,
            int imageWidth,
            RunningMode runningMode
    ) {
        this.results = poseLandmarkerResults;
        this.poseLandmarks = new PoseLandmarks(poseLandmarkerResults);
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;

        switch (runningMode) {
            case IMAGE:
            case VIDEO:
                scaleFactor = Math.min(getWidth() * 1f / this.imageWidth, getHeight() * 1f / this.imageHeight);
                break;
            case LIVE_STREAM:
                scaleFactor = Math.max(getWidth() * 1f / this.imageWidth, getHeight() * 1f / this.imageHeight);
                break;
        }
        invalidate();
    }
}
