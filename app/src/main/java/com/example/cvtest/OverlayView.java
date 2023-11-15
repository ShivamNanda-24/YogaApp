package com.example.cvtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.core.content.ContextCompat;

import com.google.mediapipe.tasks.components.containers.Connection;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import java.util.List;

public class OverlayView extends View {
    private static final float LANDMARK_STROKE_WIDTH = 12F;
    private PoseLandmarkerResult results;
    private final Paint pointPaint = new Paint();
    private final Paint linePaint = new Paint();
    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public void clear() {
        results = null;
        pointPaint.reset();
        linePaint.reset();
        invalidate();
        initPaints();
    }

    private void initPaints() {
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.mp_color_primary));
        linePaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (results != null) {
            for (List<NormalizedLandmark> landmarkList : results.landmarks()) {
                // Drawing points
                for (int i = 0; i < landmarkList.size(); i++) {
                    NormalizedLandmark landmark = landmarkList.get(i);
                    if (i == 12 || i == 14 || i == 16) {
                        Log.d(String.valueOf(i), landmark.toString());
                    }
                    else {
                        canvas.drawPoint(
                                landmark.x() * imageWidth * scaleFactor,
                                landmark.y() * imageHeight * scaleFactor,
                                pointPaint);
                    }
                }

                // Drawing lines
                for (Connection connection : PoseLandmarker.POSE_LANDMARKS) {
                    if (connection != null) {
                        float startX = landmarkList.get(connection.start()).x() * imageWidth * scaleFactor;
                        float startY = landmarkList.get(connection.start()).y() * imageHeight * scaleFactor;
                        float endX = landmarkList.get(connection.end()).x() * imageWidth * scaleFactor;
                        float endY = landmarkList.get(connection.end()).y() * imageHeight * scaleFactor;
                        canvas.drawLine(startX, startY, endX, endY, linePaint);
                    }
                }
            }
        }
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
