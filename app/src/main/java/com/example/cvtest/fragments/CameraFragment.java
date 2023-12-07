package com.example.cvtest.fragments;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.Preview;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Camera;
import androidx.camera.core.AspectRatio;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.cvtest.MainViewModel;
import com.example.cvtest.PoseLandmarkerHelper;
import com.example.cvtest.R;
import com.example.cvtest.databinding.FragmentCameraBinding;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraFragment extends Fragment implements PoseLandmarkerHelper.LandmarkerListener {
    private static final String TAG = "Pose Landmarker";
    private FragmentCameraBinding fragmentCameraBinding;
    private PoseLandmarkerHelper poseLandmarkerHelper;
    private MainViewModel viewModel;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private ExecutorService backgroundExecutor;

    @Override
    public void onResume() {
        super.onResume();
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(R.id.action_camera_to_permissions);
        }

        backgroundExecutor.execute(() -> {
            if (poseLandmarkerHelper != null) {
                if (poseLandmarkerHelper.isClose()) {
                    poseLandmarkerHelper.setupPoseLandmarker();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (poseLandmarkerHelper != null) {
            viewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence);
            viewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseDetectionConfidence);
            viewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPoseDetectionConfidence);
            viewModel.setDelegate(poseLandmarkerHelper.currentDelegate);

            backgroundExecutor.execute(() -> poseLandmarkerHelper.clearPoseLandmarker());
        }
    }

    @Override
    public void onDestroyView() {
        fragmentCameraBinding = null;
        super.onDestroyView();

        backgroundExecutor.shutdown();
        try {
            backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false);
        return fragmentCameraBinding.getRoot();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        backgroundExecutor = Executors.newSingleThreadExecutor();

        fragmentCameraBinding.viewFinder.post(() -> setUpCamera());

        backgroundExecutor.execute(() -> {
            if (poseLandmarkerHelper == null) {
                poseLandmarkerHelper = new PoseLandmarkerHelper(
                         requireContext(),
                        RunningMode.LIVE_STREAM,
                        viewModel.getCurrentMinPoseDetectionConfidence(),
                        viewModel.getCurrentMinPoseTrackingConfidence(),
                        viewModel.getCurrentMinPosePresenceConfidence(),
                        viewModel.getCurrentDelegate(),
                        this
                );
            }
        });
    }

    private void updateControlsUi() {
        if (poseLandmarkerHelper != null) {
            fragmentCameraBinding.bottomSheetLayout.detectionThresholdValue.setText(
                    String.format(Locale.US, "%.2f", poseLandmarkerHelper.minPoseDetectionConfidence)
            );
            fragmentCameraBinding.bottomSheetLayout.trackingThresholdValue.setText(
                    String.format(Locale.US, "%.2f", poseLandmarkerHelper.minPoseDetectionConfidence)
            );
            fragmentCameraBinding.bottomSheetLayout.presenceThresholdValue.setText(
                    String.format(Locale.US, "%.2f", poseLandmarkerHelper.minPoseDetectionConfidence)
            );

            backgroundExecutor.execute(() -> {
                poseLandmarkerHelper.clearPoseLandmarker();
                poseLandmarkerHelper.setupPoseLandmarker();
            });

            fragmentCameraBinding.overlay.clear();
        }
    }


    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                // Handle any exceptions that may occur when getting the camera provider.
                e.printStackTrace(); // Replace with proper error handling.
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }


    @SuppressLint("UnsafeOptInUsageError")
    private void bindCameraUseCases() {

        // CameraProvider
        CameraProvider cameraProvider = this.cameraProvider;
        if (cameraProvider == null) {
            throw new IllegalStateException("Camera initialization failed.");
        }

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraFacing)
                .build();

        // Preview
        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.getDisplay().getRotation())
                .build();

        // ImageAnalysis
        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(backgroundExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                detectPose(image);
            }
        });

        // Must unbind the use-cases before rebinding them
        ((ProcessCameraProvider) cameraProvider).unbindAll();

        try {
            // A variable number of use-cases can be passed here
            camera = ((ProcessCameraProvider) cameraProvider).bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
            );

            // Attach the viewfinder's surface provider to preview use case
            if (preview != null) {
                preview.setSurfaceProvider(fragmentCameraBinding.viewFinder.getSurfaceProvider());
            }
        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
        }
    }


    private void detectPose(ImageProxy imageProxy) {
        if (poseLandmarkerHelper != null) {

            poseLandmarkerHelper.detectLiveStream(imageProxy, cameraFacing == CameraSelector.LENS_FACING_FRONT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageAnalyzer != null) {
            imageAnalyzer.setTargetRotation(fragmentCameraBinding.viewFinder.getDisplay().getRotation());
        }
    }

    @Override
    public void onResults(PoseLandmarkerHelper.ResultBundle resultBundle) {
        getActivity().runOnUiThread(() -> {
            if (fragmentCameraBinding != null) {
                fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.setText(
                        String.format(Locale.US, "%d ms", resultBundle.getInferenceTime())
                );

                fragmentCameraBinding.overlay.setResults(
                        resultBundle.getResult(),
                        resultBundle.getInputImageHeight(),
                        resultBundle.getInputImageWidth(),
                        RunningMode.LIVE_STREAM
                );

                fragmentCameraBinding.overlay.invalidate();
            }
        });
    }

    @Override
    public void onError(String error, int errorCode) {
        getActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            if (errorCode == PoseLandmarkerHelper.GPU_ERROR) {
                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                        PoseLandmarkerHelper.DELEGATE_CPU, false
                );
            }
        });
    }
}
