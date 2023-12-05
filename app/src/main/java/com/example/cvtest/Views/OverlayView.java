package com.example.cvtest.Views;

import static com.example.cvtest.features.PoseEstimation.calculateAngle;
import static com.example.cvtest.features.PoseEstimation.calculateDistance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cvtest.features.CustomTimer;
import com.example.cvtest.features.PoseEstimation;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.Arrays;
import java.util.List;

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (results != null) {
            for (List<NormalizedLandmark> landmarkList : results.landmarks()) {
                boolean isAnyLandmarkLessVisible = true;
//                boolean isAnyLandmarkLessVisible = landmarkList.stream()
//                        .anyMatch(landmark -> landmark.visibility().orElse(1.0f) < 0.50f);
                if (isAnyLandmarkLessVisible) {
                    canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);

                    // Draw white text in the middle of the canvas
                    float centerY = getHeight() / 2f;
                    float firstLineY = centerY - warningTextPaint.getTextSize();
                    float secondLineY = centerY;

                    // Draw two lines of text
                    canvas.drawText("Please take a few", getWidth() / 2f, firstLineY, warningTextPaint);
                    canvas.drawText("steps back", getWidth() / 2f, secondLineY, warningTextPaint);
                }
                else {
                    NormalizedLandmark nose = landmarkList.get(0);
                    NormalizedLandmark leftShoulder = landmarkList.get(12);
                    NormalizedLandmark leftElbow = landmarkList.get(14);
                    NormalizedLandmark leftHandPinky = landmarkList.get(18);
                    NormalizedLandmark leftHandMiddle = landmarkList.get(20);
                    NormalizedLandmark leftHandThumb = landmarkList.get(22);
                    NormalizedLandmark leftWrist = landmarkList.get(16);
                    NormalizedLandmark rightHandPinky = landmarkList.get(17);
                    NormalizedLandmark rightHandMiddle = landmarkList.get(19);
                    NormalizedLandmark rightHandThumb = landmarkList.get(21);
                    NormalizedLandmark rightShoulder = landmarkList.get(11);
                    NormalizedLandmark rightElbow = landmarkList.get(13);
                    NormalizedLandmark rightWrist = landmarkList.get(15);
                    NormalizedLandmark rightHip = landmarkList.get(23);
                    NormalizedLandmark rightKnee = landmarkList.get(25);
                    NormalizedLandmark leftKnee = landmarkList.get(26);
                    NormalizedLandmark rightAnkle = landmarkList.get(27);
                    NormalizedLandmark rightFoot = landmarkList.get(29);

                    List<NormalizedLandmark> leftArmLandmarks = Arrays.asList(leftShoulder, leftElbow, leftWrist);
                    List<NormalizedLandmark> rightArmLandmarks = Arrays.asList(rightShoulder, rightElbow, rightWrist);
                    List<NormalizedLandmark> rightLegLandmarks = Arrays.asList(rightHip, rightKnee, rightFoot);
                    List<NormalizedLandmark> leftHandLandmarks = Arrays.asList(leftHandPinky, leftHandMiddle, leftHandThumb);
                    List<NormalizedLandmark> rightHandLandmarks = Arrays.asList(rightHandPinky, rightHandMiddle, rightHandThumb);

                    // initial drawings
                    drawLine(canvas, leftArmLandmarks, incorrectPaint);
                    drawLine(canvas, rightArmLandmarks, incorrectPaint);
                    drawLine(canvas, rightLegLandmarks, incorrectPaint);
                    drawLine(canvas, leftHandLandmarks, incorrectPaint);
                    drawLine(canvas, rightHandLandmarks, incorrectPaint);
                    canvas.drawPoint(leftKnee.x() * imageWidth * scaleFactor, leftKnee.y() * imageHeight * scaleFactor, incorrectPointPaint);

                    float distPalms = calculateDistance(rightWrist, leftWrist);
                    float distLeftKneeAndRightFoot = calculateDistance(leftKnee, rightFoot);
                    float angleRightArm = calculateAngle(rightArmLandmarks);
                    float angleLeftArm = calculateAngle(leftArmLandmarks);
                    float angleRightLeg = calculateAngle(rightLegLandmarks);

                    // firstCheck - if hands are fully extended and joined
                    // distance is less than 0.2 -
                    boolean wristsAboveNose = rightWrist.y() < nose.y() && leftWrist.y() < nose.y();
                    boolean armsPosition = checkArmsPosition(canvas, distPalms, leftHandLandmarks,rightHandLandmarks);
                    boolean armsStretched = checkArmsStretched(canvas, angleRightArm,angleLeftArm,wristsAboveNose,leftArmLandmarks,rightArmLandmarks);
                    boolean rightLegAngle = checkRightLegAngle(canvas,angleRightLeg, rightLegLandmarks);
                    boolean kneeFootDistance = checkKneeFootDistance(canvas, distLeftKneeAndRightFoot, leftKnee);

                    if (armsPosition && armsStretched && kneeFootDistance && rightLegAngle) {
                        setMessage("HOLD");
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
            }
        }
    }
    private void initCustomTimer() {
        customTimer = new CustomTimer(30000, 1000, new CustomTimer.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimer(String.valueOf(millisUntilFinished/1000));
            }
        });
    }
    private boolean checkArmsPosition(Canvas canvas, Float distPalms,List<NormalizedLandmark> leftHandLandmarks, List<NormalizedLandmark> rightHandLandmarks ) {
        if (distPalms < 0.1) {
            drawLine(canvas, leftHandLandmarks, correctPaint);
            drawLine(canvas, rightHandLandmarks, correctPaint);
            return true;
        } else {
            drawLine(canvas, leftHandLandmarks, incorrectPaint);
            drawLine(canvas, rightHandLandmarks, incorrectPaint);
            setMessage("Put both hands together");
            return false;
        }
    }

    private boolean checkArmsStretched(Canvas canvas, Float angleRightArm, Float angleLeftArm, Boolean wristsAboveNose,List<NormalizedLandmark> leftArmLandmarks, List<NormalizedLandmark> rightArmLandmarks ) {
        if (angleRightArm < 90 && angleLeftArm < 90 && wristsAboveNose) {
            drawLine(canvas, leftArmLandmarks, correctPaint);
            drawLine(canvas, rightArmLandmarks, correctPaint);
            return true;
        } else {
            drawLine(canvas, leftArmLandmarks, incorrectPaint);
            drawLine(canvas, rightArmLandmarks, incorrectPaint);
            setMessage("Stretch both your hands over your head");
            return false;
        }
    }

    private boolean checkKneeFootDistance(Canvas canvas, Float distLeftKneeAndRightFoot, NormalizedLandmark leftKnee) {
        if (distLeftKneeAndRightFoot < 0.1) {
            canvas.drawPoint(leftKnee.x() * imageWidth * scaleFactor,
                    leftKnee.y() * imageHeight * scaleFactor, correctPointPaint);
            return true;
        } else {
            canvas.drawPoint(leftKnee.x() * imageWidth * scaleFactor,
                    leftKnee.y() * imageHeight * scaleFactor, incorrectPointPaint);
            setMessage("Place your right foot closer to your left knee");
            return false;
        }
    }

    private boolean checkRightLegAngle(Canvas canvas, Float angleRightLeg,List<NormalizedLandmark> rightLegLandmarks ) {
        if (angleRightLeg > 130) {
            drawLine(canvas, rightLegLandmarks, correctPaint);
            return true;
        } else {
            drawLine(canvas, rightLegLandmarks, incorrectPaint);
            setMessage("Stretch right leg outwards");
            return false;
        }
    }




    private void drawLine(Canvas canvas, List<NormalizedLandmark> landmarks, Paint paint) {
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
