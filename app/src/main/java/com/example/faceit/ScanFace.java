package com.example.faceit;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanFace extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private PreviewView previewView;
    private Button finishButton;
    private ImageCapture imageCapture;
    private boolean isCameraReady = false;

    private final String[] basePermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    private final String[] storagePermissionsBelow13 = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final String[] mediaPermissions13Plus = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_face);

        previewView = findViewById(R.id.previewView);
        finishButton = findViewById(R.id.finishButton);

        finishButton.setOnClickListener(v -> {
            if (isCameraReady) {
                captureAndSend();
            } else {
                Toast.makeText(ScanFace.this, "Camera not ready yet. Please wait...", Toast.LENGTH_SHORT).show();
            }
        });

        requestAllPermissions();
    }

    private void requestAllPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        permissionsToRequest.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        List<String> missing = new ArrayList<>();
        for (String perm : permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                missing.add(perm);
            }
        }

        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) this,
                        cameraSelector,
                        preview,
                        imageCapture);

                isCameraReady = true;

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndSend() {
        File photoFile = new File(getCacheDir(), "captured_face.jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(ScanFace.this, "Image captured. Sending to server...", Toast.LENGTH_SHORT).show();
                        uploadToBackend(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(ScanFace.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("CaptureError", "Failed to capture image", exception);
                    }
                });
    }

    private void uploadToBackend(File imageFile) {
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", Context.MODE_PRIVATE);
        String name = prefs.getString("student_name", "Unknown");
        String matric = prefs.getString("student_matric", "N/A");

        ApiService api = ApiClient.getClient("http://192.168.1.111:8000/").create(ApiService.class);

        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody matricBody = RequestBody.create(MediaType.parse("text/plain"), matric);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), reqFile);

        Call<ApiResponse> call = api.registerStudent(nameBody, matricBody, body);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ScanFace.this, response.body().message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ScanFace.this, "Registration failed: No face found or invalid image.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ScanFace.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    Log.d("PermissionCheck", "Permission denied: " + permissions[i]);
                } else {
                    Log.d("PermissionCheck", "Permission granted: " + permissions[i]);
                }
            }

            if (allGranted) {
                startCamera();
            } else {
                Toast.makeText(this, "All permissions are required to continue.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
