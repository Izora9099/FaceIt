package com.example.faceit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.concurrent.ExecutionException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BasicAttendanceActivity extends AppCompatActivity {

    // UI Components
    private TextView courseInfoText, instructionText, resultText;
    private MaterialButton captureButton, backButton;
    private PreviewView previewView;

    // Camera
    private ImageCapture imageCapture;
    private boolean isCameraReady = false;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // Course data
    private String courseId;
    private String courseName;
    private String courseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_attendance);

        // Get course data from intent
        courseId = getIntent().getStringExtra("course_id");
        courseName = getIntent().getStringExtra("course_name");
        courseCode = getIntent().getStringExtra("course_code");

        if (courseId == null) {
            Toast.makeText(this, "❌ Error: No course selected", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize UI
        initializeViews();
        setupCourseInfo();

        // Request camera permissions
        requestCameraPermissions();

        // Setup button listeners
        captureButton.setOnClickListener(v -> captureAndRecognize());
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        courseInfoText = findViewById(R.id.course_info_text);
        instructionText = findViewById(R.id.instruction_text);
        resultText = findViewById(R.id.result_text);
        captureButton = findViewById(R.id.capture_button);
        backButton = findViewById(R.id.back_button);
        previewView = findViewById(R.id.preview_view);
    }

    private void setupCourseInfo() {
        courseInfoText.setText("📚 " + courseName);
        instructionText.setText("👤 Position your face in the camera and tap 'Capture' to mark attendance");
        resultText.setText("Ready to scan...");
    }

    private void requestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            setupCamera();
        }
    }

    private void setupCamera() {
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
                captureButton.setEnabled(true);
                resultText.setText("📷 Camera ready - tap 'Capture' to scan");

                Log.d("BasicAttendance", "✅ Camera setup completed");

            } catch (ExecutionException | InterruptedException e) {
                Log.e("BasicAttendance", "❌ Error starting camera", e);
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndRecognize() {
        if (!isCameraReady) {
            Toast.makeText(this, "Camera not ready yet...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button and show loading
        captureButton.setEnabled(false);
        captureButton.setText("Processing...");
        resultText.setText("🔄 Capturing and processing...");

        File photoFile = new File(getCacheDir(), "attendance_capture.jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("BasicAttendance", "✅ Image captured, sending to Django...");
                        uploadToBackend(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("BasicAttendance", "❌ Capture failed", exception);

                        // Reset button state
                        captureButton.setEnabled(true);
                        captureButton.setText("📸 Capture");
                        resultText.setText("❌ Capture failed. Try again.");

                        Toast.makeText(BasicAttendanceActivity.this,
                                "Capture failed: " + exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadToBackend(File imageFile) {
        // ✅ Use existing Django recognize-face endpoint with course_id
        ApiService api = ApiClient.getClient().create(ApiService.class);

        RequestBody courseIdBody = RequestBody.create(MediaType.parse("text/plain"), courseId);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), reqFile);

        Call<ApiResponse> call = api.takeAttendance(body, courseIdBody);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // Reset button state
                captureButton.setEnabled(true);
                captureButton.setText("📸 Capture");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    Log.d("BasicAttendance", "✅ Recognition response: " + apiResponse.message);

                    // Show success result
                    resultText.setText("✅ " + apiResponse.message);
                    Toast.makeText(BasicAttendanceActivity.this,
                            apiResponse.message,
                            Toast.LENGTH_LONG).show();

                } else {
                    Log.e("BasicAttendance", "❌ Recognition failed. Response code: " + response.code());

                    String errorMsg = "❌ Recognition failed";
                    if (response.code() == 400) {
                        errorMsg += " - No face detected or student not found";
                    } else if (response.code() == 401) {
                        errorMsg += " - Authentication required";
                    }

                    resultText.setText(errorMsg);
                    Toast.makeText(BasicAttendanceActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Reset button state
                captureButton.setEnabled(true);
                captureButton.setText("📸 Capture");

                Log.e("BasicAttendance", "❌ Network error", t);

                String errorMsg = "❌ Network error: " + t.getMessage();
                resultText.setText(errorMsg);
                Toast.makeText(BasicAttendanceActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            } else {
                Toast.makeText(this, "Camera permission required for attendance",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}