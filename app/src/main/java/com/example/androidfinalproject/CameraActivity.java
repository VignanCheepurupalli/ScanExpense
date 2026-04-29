package com.example.androidfinalproject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Read which flow to run: SCAN vs OCR
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "SCAN";
        Toast.makeText(this, "Camera mode: " + mode, Toast.LENGTH_LONG).show();

        previewView = findViewById(R.id.previewView);
        Button captureButton = findViewById(R.id.captureButton);

        // Request camera permission if needed
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.CAMERA},
                    100
            );
        } else {
            startCamera();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        captureButton.setOnClickListener(view -> takePhoto());
    }

    private void runTextRecognition(Uri imageUri) {
        // Kick off OCR
        OcrProcessor.extractTextFromImage(this, imageUri, new OcrProcessor.OcrCallback() {
            @Override
            public void onTextExtracted(String result) {
                // Show the result and only finish when user taps OK
                new AlertDialog.Builder(CameraActivity.this)
                        .setTitle("Extracted Text")
                        .setMessage(result)
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Now finish the activity
                            CameraActivity.this.finish();
                        })
                        .setCancelable(false)  // prevent dismissing by tapping outside
                        .show();
            }

            @Override
            public void onError(Exception e) {
                new AlertDialog.Builder(CameraActivity.this)
                        .setTitle("OCR Error")
                        .setMessage(e.getMessage())
                        .setPositiveButton("OK", (dialog, which) -> {
                            CameraActivity.this.finish();
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture
                );

            } catch (Exception e) {
                Log.e("CameraX", "Camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(
                getCacheDir(),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        Log.d("CameraX", "Photo saved: " + savedUri);
                        Log.d("CameraActivity", "Mode is: " + mode);

                        if ("OCR".equals(mode)) {
                            runTextRecognition(savedUri);
                            return;  // skip returning to MainActivity
                        }

                        // SCAN flow: launch ReviewReceiptActivity
                        Intent intent = new Intent(CameraActivity.this, ReviewReceiptActivity.class);
                        intent.putExtra("image_uri", savedUri.toString());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed", exception);
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(
                    this,
                    "Camera permission is required to use this feature",
                    Toast.LENGTH_LONG
            ).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
