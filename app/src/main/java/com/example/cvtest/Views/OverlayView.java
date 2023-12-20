package com.example.cvtest.Views;


import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cvtest.MainActivity;
import com.example.cvtest.features.CustomTimer;
import com.example.cvtest.features.PoseEstimation;
import com.example.cvtest.helpers.PoseLandmarks;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private static String buttonPressedValue;
    private PoseLandmarks poseLandmarks;
    private float midPointLeftTorso;
    private float midPointRightTorso;
    private boolean showGoMessage = false;
    private String messageToShow;
    private static final MutableLiveData<Boolean> timerPaused = new MutableLiveData<>();
    private boolean shownOnce = false;

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
        customTimer.reset(30000);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (poseLandmarks != null) {

            if (allLandmarksVisible(poseLandmarks)) {

                Map<String, Object> poseAnalysisResult = PoseEstimation.generatePoseLines(buttonPressedValue, poseLandmarks);
                Map<String, Object> poseAnalysisCalculations = PoseEstimation.computeYogaPose(buttonPressedValue, poseLandmarks);
                Map<String, Object> getYogaAngles = PoseEstimation.yogaPoseCalculations(buttonPressedValue, poseLandmarks);

                if (buttonPressedValue.equals("Tree Pose")) {
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

                    if (rightLegAngle) {
                        drawLine(canvas, rightLegLandmarks, correctPointPaint);
                    } else {
                        drawLine(canvas, rightLegLandmarks, incorrectPaint);
                        setMessage("Stretch right leg outwards");
                    }

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

                    if (showGoMessage) {
                        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);
                        canvas.drawText(messageToShow, getWidth() / 2f, getHeight() / 2f, warningTextPaint);
                    }


                    if (armsPosition && armsStretched && kneeFootDistance && rightLegAngle) {
                        setMessage("HOLD");
                        // Make a ready set go
                        if (!customTimer.isRunning()) {
                            if (!shownOnce){
                                showGoMessage();
                                shownOnce = true;
                            }
                            else{
                                customTimer.start();
                                setTimerStatus(false);
                            }
                        }
                    } else {
                        if (customTimer.isRunning()) {
                            customTimer.pause();
                            setMessage("Paused");
                            setTimerStatus(true);
                        }
                    }
                }
                else if (buttonPressedValue.equals("Warrior Pose") || buttonPressedValue.equals("Routine")) {

                    if (poseLandmarks.leftShoulder != null && poseLandmarks.leftHip != null) {
                        midPointLeftTorso = (poseLandmarks.leftShoulder.y() + poseLandmarks.leftHip.y()) / 2;
                    }
                    if (poseLandmarks.rightShoulder != null && poseLandmarks.rightHip != null) {
                        midPointRightTorso = (poseLandmarks.rightShoulder.y() + poseLandmarks.rightHip.y()) / 2;
                    }

                    boolean rightArmStretched = (boolean) poseAnalysisCalculations.get("rightArmStretched") && (poseLandmarks.rightWrist.y() > poseLandmarks.nose.y())  && (poseLandmarks.rightWrist.y() < midPointRightTorso);
                    boolean leftArmStretched = (boolean) poseAnalysisCalculations.get("leftArmStretched") && (poseLandmarks.leftWrist.y() > poseLandmarks.nose.y()) && (poseLandmarks.leftWrist.y() < midPointLeftTorso);
                    boolean leftLegAngle = (boolean) poseAnalysisCalculations.get("leftLegAngle");
                    boolean rightLegAngle = (boolean) poseAnalysisCalculations.get("rightLegAngle");

                    List<NormalizedLandmark> leftArmLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("leftArmLandmarks");
                    List<NormalizedLandmark> rightArmLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("rightArmLandmarks");
                    List<NormalizedLandmark> rightLegLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("rightLegLandmarks");
                    List<NormalizedLandmark> leftLegLandmarks = (List<NormalizedLandmark>) poseAnalysisResult.get("leftLegLandmarks");
                    boolean feetDistance = (boolean) poseAnalysisCalculations.get("feetDistance");


                    if (poseLandmarks.rightWrist != null && poseLandmarks.nose != null) {

                        if (rightArmStretched ) {
                            drawLine(canvas, rightArmLandmarks, correctPointPaint);
                        } else {
                            drawLine(canvas, rightArmLandmarks, incorrectPaint);
                            setMessage("Stretch your right hand outwards");
                        }
                    }

                    if (poseLandmarks.leftWrist != null && poseLandmarks.nose != null) {
                        if (leftArmStretched) {
                            drawLine(canvas, leftArmLandmarks, correctPointPaint);
                        } else {
                            drawLine(canvas, leftArmLandmarks, incorrectPaint);
                            setMessage("Stretch your left hand outwards");
                        }
                    }

                    if (rightLegAngle) {
                        if(poseLandmarks.leftHeel!= null && poseLandmarks.rightHeel != null ) {
                            drawLine(canvas, rightLegLandmarks, correctPaint);
                        }

                    } else {
                        drawLine(canvas, rightLegLandmarks, incorrectPaint);
                        setMessage("Open your right leg outwards and bend right knee");
                    }

                    if (leftLegAngle) {
                        if(poseLandmarks.leftHeel!= null && poseLandmarks.rightHeel != null ) {
                            drawLine(canvas, leftLegLandmarks, correctPaint);
                        }

                    } else {
                        drawLine(canvas, leftLegLandmarks, incorrectPaint);
                        setMessage("Ensure your left leg is stretched straight");
                    }

                    if (poseLandmarks.rightFootIndex != null && poseLandmarks.leftFootIndex != null){
                        if (feetDistance) {
                            canvas.drawPoint(poseLandmarks.leftFootIndex.x() * imageWidth * scaleFactor, poseLandmarks.leftFootIndex.y() * imageHeight * scaleFactor, correctPointPaint);
                            canvas.drawPoint(poseLandmarks.rightFootIndex.x() * imageWidth * scaleFactor, poseLandmarks.rightFootIndex.y()  * imageHeight * scaleFactor, correctPointPaint);

                        } else {
                            canvas.drawPoint(poseLandmarks.leftFootIndex.x() * imageWidth * scaleFactor, poseLandmarks.leftFootIndex.y()  * imageHeight * scaleFactor, incorrectPointPaint);
                            canvas.drawPoint(poseLandmarks.rightFootIndex.x() * imageWidth * scaleFactor, poseLandmarks.rightFootIndex.y()  * imageHeight * scaleFactor, incorrectPointPaint);

                            setMessage("Open your feet wider");
                        }
                    }

                    if (showGoMessage) {
                        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);
                        canvas.drawText(messageToShow, getWidth() / 2f, getHeight() / 2f, warningTextPaint);
                    }

                    if (rightArmStretched && leftArmStretched && leftLegAngle && rightLegAngle && feetDistance) {
                        setMessage("HOLD");
                        // Make a ready set go
                        if (!customTimer.isRunning()) {
                            if (!shownOnce){
                                showGoMessage();
                                shownOnce = true;
                            }
                            else{
                                customTimer.start();
                                setTimerStatus(false);
                            }
                        }
                    } else {
                        if (customTimer.isRunning()) {
                            customTimer.pause();
                            setMessage("Paused");
                            setTimerStatus(true);

                        }
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
        }
    }

    private void drawAngles(Canvas canvas, Float angle, NormalizedLandmark mark){
        if (mark != null) {
            canvas.drawText(angle.toString(), mark.x() * imageWidth * scaleFactor,
                    mark.y() * imageHeight * scaleFactor, textPaint);
        }

    }

    private boolean allLandmarksVisible(PoseLandmarks poseLandmarks) {
        if (poseLandmarks == null || poseLandmarks.landmarks == null) {
            return false;
        }
            // Iterate over all the landmarks
            for (NormalizedLandmark landmark : poseLandmarks.landmarks) {
                Optional<Float> visibility = landmark.visibility();

                if (!visibility.isPresent() || visibility.get() < 0.4f) {
                    return false;
                }
            }
        return true;
    }

    private void showGoMessage() {
        showMessage("Ready");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showMessage("Set");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showMessage("Go");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                clearMessage();
                                customTimer.start();
                                setTimerStatus(false);
                            }
                        }, 1000); // Display "Go" for 1 second then start the timer
                    }
                }, 1000); // Display "Set" for 1 second
            }
        }, 1000); // Display "Ready" for 1 second
    }

    private void showMessage(String message) {
        showGoMessage = true;
        messageToShow = message; // 'messageToShow' is a new String field
        invalidate(); // Trigger a redraw to show the message
    }

    private void clearMessage() {
        showGoMessage = false;
        invalidate(); // Trigger a redraw to clear the message
    }

    public static void setButtonPressedValue(String value) {
        buttonPressedValue = value;
    }

    private void initCustomTimer() {
        customTimer = new CustomTimer(15000, 1000, new CustomTimer.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimer(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                if (buttonPressedValue.equals("Routine")){
                    customTimer.reset(15000);
                    Intent intent = new Intent(OverlayView.this.getContext(), MainActivity.class);
                    intent.putExtra("button_pressed", "Tree Pose");
                    getContext().startActivity(intent);
                }
                else {
                    Intent intent = new Intent(getContext(), CompletionPage.class);
                    getContext().startActivity(intent);
                }
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

    public static LiveData<Boolean> getTimerStatus() {
        return timerPaused;
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

    public void setTimerStatus(Boolean timerStatus) {
        timerPaused.postValue(timerStatus);
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
