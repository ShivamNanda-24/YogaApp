package com.example.cvtest.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.camera.core.ImageProxy;
import com.example.cvtest.fragments.CameraFragment;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import java.nio.ByteBuffer;

public class PoseLandmarkerHelper {
    public static final int MODEL_POSE_LANDMARKER_FULL = 0;
    public static final int DELEGATE_CPU = 0;
    private static final String TAG = "PoseLandmarkerHelper";
    public static final int DELEGATE_GPU = 1;
    public static final float DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5f;
    public static final float DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5f;
    public static final float DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5f;
    public static final int DEFAULT_NUM_POSES = 1;
    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;
    public static final int MODEL_POSE_LANDMARKER_LITE = 1;
    public static final int MODEL_POSE_LANDMARKER_HEAVY = 2;


    public float minPoseDetectionConfidence;
    private float minPoseTrackingConfidence;
    private float minPosePresenceConfidence;
    private int currentModel;
    public int currentDelegate;
    private RunningMode runningMode;
    private Context context;
    private LandmarkerListener poseLandmarkerHelperListener;
    private PoseLandmarker poseLandmarker;



    public PoseLandmarkerHelper(Context requireContext, com.google.mediapipe.tasks.vision.core.RunningMode liveStream, float currentMinPoseDetectionConfidence, float currentMinPoseTrackingConfidence, float currentMinPosePresenceConfidence, int currentDelegate,LandmarkerListener poseLandmarkerHelperListener) {
        this.context = requireContext;
        this.minPoseDetectionConfidence = DEFAULT_POSE_DETECTION_CONFIDENCE;
        this.minPoseTrackingConfidence = DEFAULT_POSE_TRACKING_CONFIDENCE;
        this.minPosePresenceConfidence = DEFAULT_POSE_PRESENCE_CONFIDENCE;
        this.currentModel = MODEL_POSE_LANDMARKER_FULL;
        this.currentDelegate = DELEGATE_CPU;
        this.runningMode = RunningMode.LIVE_STREAM;
        this.poseLandmarkerHelperListener = poseLandmarkerHelperListener;
        setupPoseLandmarker();
    }

    public void clearPoseLandmarker() {
        if (poseLandmarker != null) {
            poseLandmarker.close();
            poseLandmarker = null;
        }
    }

    public boolean isClose() {
        return poseLandmarker == null;
    }

    public void setupPoseLandmarker() {
        BaseOptions.Builder baseOptionBuilder = BaseOptions.builder();

        switch (currentDelegate) {
            case DELEGATE_CPU:
                baseOptionBuilder.setDelegate(Delegate.CPU);
                break;
            case DELEGATE_GPU:
                baseOptionBuilder.setDelegate(Delegate.GPU);
                break;
        }

        String modelName;
        switch (currentModel) {
            case MODEL_POSE_LANDMARKER_FULL:
                modelName = "pose_landmarker_full.task";
                break;
            case MODEL_POSE_LANDMARKER_LITE:
                modelName = "pose_landmarker_lite.task";
                break;
            case MODEL_POSE_LANDMARKER_HEAVY:
                modelName = "pose_landmarker_heavy.task";
                break;
            default:
                modelName = "pose_landmarker_full.task";
                break;
        }

        baseOptionBuilder.setModelAssetPath(modelName);

        if (runningMode == RunningMode.LIVE_STREAM && poseLandmarkerHelperListener == null) {
            throw new IllegalStateException("poseLandmarkerHelperListener must be set when runningMode is LIVE_STREAM.");
        }

        try {
            BaseOptions baseOptions = baseOptionBuilder.build();
            PoseLandmarker.PoseLandmarkerOptions.Builder optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                    .setMinTrackingConfidence(minPoseTrackingConfidence)
                    .setMinPosePresenceConfidence(minPosePresenceConfidence)
                    .setRunningMode(com.google.mediapipe.tasks.vision.core.RunningMode.LIVE_STREAM);

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder.setResultListener(this::returnLivestreamResult)
                        .setErrorListener(this::returnLivestreamError);
            }

            PoseLandmarker.PoseLandmarkerOptions options = optionsBuilder.build();
            poseLandmarker = PoseLandmarker.createFromOptions(context, options);
        } catch (IllegalStateException e) {
            if (poseLandmarkerHelperListener != null) {
                poseLandmarkerHelperListener.onError("Pose Landmarker failed to initialize. See error logs for details", GPU_ERROR);
            }
            Log.e(TAG, "MediaPipe failed to load the task with error: " + e.getMessage());
        } catch (RuntimeException e) {
            if (poseLandmarkerHelperListener != null) {
                poseLandmarkerHelperListener.onError("Pose Landmarker failed to initialize. See error logs for details", GPU_ERROR);
            }
            Log.e(TAG, "Image classifier failed to load model with error: " + e.getMessage());
        }
    }

    public void detectLiveStream(ImageProxy imageProxy, boolean isFrontCamera) {
        Log.d("msg", runningMode.toString());
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw new IllegalArgumentException("Attempting to call detectLiveStream while not using RunningMode.LIVE_STREAM");
        }

        long frameTime = SystemClock.uptimeMillis();
        Bitmap bitmapBuffer = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);

        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        bitmapBuffer.copyPixelsFromBuffer(buffer);
        imageProxy.close();

        Matrix matrix = new Matrix();
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

        if (isFrontCamera) {
            matrix.postScale(-1f, 1f, imageProxy.getWidth() / 2f, imageProxy.getHeight() / 2f);
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);

        MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();
        detectAsync(mpImage, frameTime);
    }


    @VisibleForTesting
    public void detectAsync(MPImage mpImage, long frameTime) {
        if (poseLandmarker != null) {
            poseLandmarker.detectAsync(mpImage, frameTime);
        }
    }

    private void returnLivestreamResult(PoseLandmarkerResult result, MPImage input) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();

        if (poseLandmarkerHelperListener != null) {
            poseLandmarkerHelperListener.onResults(new ResultBundle(result, inferenceTime, input.getHeight(), input.getWidth()));
        }
    }

    private void returnLivestreamError(RuntimeException error) {
        if (poseLandmarkerHelperListener != null) {
            poseLandmarkerHelperListener.onError(error.getMessage() != null ? error.getMessage() : "An unknown error has occurred", GPU_ERROR);
        }
    }

    public interface LandmarkerListener {
        void onError(String error, int errorCode);

        void onResults(ResultBundle resultBundle);
    }

    public static class ResultBundle {
        private final PoseLandmarkerResult result;
        private final long inferenceTime;
        private final int inputImageHeight;
        private final int inputImageWidth;

        public ResultBundle(PoseLandmarkerResult result, long inferenceTime, int inputImageHeight, int inputImageWidth) {
            this.result = result;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }

        public PoseLandmarkerResult getResult() {
            return result;
        }

        public long getInferenceTime() {
            return inferenceTime;
        }

        public int getInputImageHeight() {
            return inputImageHeight;
        }

        public int getInputImageWidth() {
            return inputImageWidth;
        }


    }

    public enum RunningMode {
        IMAGE,
        LIVE_STREAM
    }
}
