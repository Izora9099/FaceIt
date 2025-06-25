package com.example.faceit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
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
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceSessionActivity extends AppCompatActivity {

    // UI Components
    private TextView courseNameText, sessionTimeText, attendanceCountText;
    private TextView sessionStatusText;
    private MaterialButton endSessionButton;
    private RecyclerView recentCheckinsRecycler;
    private PreviewView previewView;

    // Camera
    private ImageCapture imageCapture;
    private boolean isCameraReady = false;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // Session Management
    private String courseId;
    private String sessionId;
    private long sessionStartTime;
    private int sessionDuration; // in minutes
    private int gracePeriod = 15; // in minutes - configurable
    private Timer sessionTimer;
    private Timer updateTimer;
    private boolean isSessionActive = true;

    // Data
    private List<RecentCheckIn> recentCheckIns = new ArrayList<>();
    private RecentCheckInsAdapter adapter;

    // Handler for UI updates
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_session);

        // Initialize UI components
        initializeViews();

        // Get course details from intent
        courseId = getIntent().getStringExtra("course_id");
        sessionDuration = getIntent().getIntExtra("session_duration", 120); // default 2 hours
        String courseName = getIntent().getStringExtra("course_name");

        if (courseId == null) {
            Toast.makeText(this, "Error: No course selected", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set course name
        courseNameText.setText(courseName != null ? courseName : "Unknown Course");

        // Setup RecyclerView
        setupRecyclerView();

        // Request permissions and setup camera
        requestCameraPermissions();

        // Start attendance session on server
        startServerSession();

        // Setup button listeners
        endSessionButton.setOnClickListener(v -> endSessionManually());
    }

    private void initializeViews() {
        courseNameText = findViewById(R.id.course_name_text);
        sessionTimeText = findViewById(R.id.session_time_text);
        attendanceCountText = findViewById(R.id.attendance_count_text);
        sessionStatusText = findViewById(R.id.session_status_text);
        endSessionButton = findViewById(R.id.end_session_button);
        recentCheckinsRecycler = findViewById(R.id.recent_checkins_recycler);
        previewView = findViewById(R.id.previewView);
    }

    private void setupRecyclerView() {
        adapter = new RecentCheckInsAdapter(recentCheckIns);
        recentCheckinsRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentCheckinsRecycler.setAdapter(adapter);
    }

    private void requestCameraPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
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
                Log.d("AttendanceSession", "Camera setup completed");

                // Enable auto-capture for continuous scanning
                startAutoCaptureForAttendance();

            } catch (ExecutionException | InterruptedException e) {
                Log.e("AttendanceSession", "Error starting camera", e);
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startAutoCaptureForAttendance() {
        // Auto-capture every 3 seconds for face recognition
        Timer captureTimer = new Timer();
        captureTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isCameraReady && isSessionActive) {
                    mainHandler.post(() -> captureForAttendance());
                }
            }
        }, 2000, 3000); // Start after 2 seconds, repeat every 3 seconds
    }

    private void captureForAttendance() {
        if (!isCameraReady || !isSessionActive) {
            return;
        }

        File photoFile = new File(getCacheDir(), "attendance_capture_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Process the captured image for attendance
                        handleStudentCheckIn(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("AttendanceSession", "Auto capture failed", exception);
                    }
                });
    }

    private void startServerSession() {
        // 🌐 NETWORK: Using IP from ApiClient configuration
        // If this fails, check if IP address in ApiClient.java matches your computer's current IP
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // Get teacher ID from SharedPreferences or Intent
        String teacherId = getSharedPreferences("faceit_prefs", MODE_PRIVATE)
                .getString("teacher_id", "");

        Call<SessionResponse> call = api.startAttendanceSession(courseId, teacherId);
        call.enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionResponse sessionResponse = response.body();
                    sessionId = sessionResponse.getSessionId();
                    gracePeriod = sessionResponse.getGracePeriodMinutes();

                    Toast.makeText(AttendanceSessionActivity.this,
                            "Session started successfully", Toast.LENGTH_SHORT).show();

                    // Start session timer and real-time updates
                    startSessionTimer();
                    startRealTimeUpdates();

                } else {
                    Toast.makeText(AttendanceSessionActivity.this,
                            "Failed to start session", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Toast.makeText(AttendanceSessionActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void startSessionTimer() {
        sessionStartTime = System.currentTimeMillis();
        sessionTimer = new Timer();

        sessionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainHandler.post(() -> updateTimeDisplays());
            }
        }, 0, 1000); // Update every second
    }

    private void updateTimeDisplays() {
        if (!isSessionActive) return;

        long elapsed = System.currentTimeMillis() - sessionStartTime;
        long remaining = (sessionDuration * 60 * 1000) - elapsed;

        if (remaining <= 0) {
            // Session expired - auto-close
            endSessionAutomatically();
        } else {
            // Update UI with session timing
            updateSessionUI(elapsed, remaining);
        }
    }

    private void updateSessionUI(long elapsedMs, long remainingMs) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String startTime = timeFormat.format(new Date(sessionStartTime));
        String endTime = timeFormat.format(new Date(sessionStartTime + (sessionDuration * 60 * 1000)));

        sessionTimeText.setText(String.format("Started: %s | Ends: %s", startTime, endTime));

        long elapsedMinutes = elapsedMs / (60 * 1000);
        long remainingMinutes = remainingMs / (60 * 1000);

        String statusText;
        if (elapsedMinutes <= gracePeriod) {
            long graceRemaining = gracePeriod - elapsedMinutes;
            statusText = String.format("Grace Period: %d minutes remaining", graceRemaining);
        } else {
            statusText = String.format("Late Period: %d minutes remaining", remainingMinutes);
        }

        sessionStatusText.setText(statusText);
    }

    private void handleStudentCheckIn(File faceImage) {
        if (!isSessionActive || sessionId == null) {
            return;
        }

        // Determine attendance status based on timing
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        String attendanceStatus = determineAttendanceStatus(elapsed);

        if ("SESSION_ENDED".equals(attendanceStatus)) {
            // Don't process check-ins after session ends
            return;
        }

        // Send to server with session context
        uploadAttendanceWithSession(faceImage, attendanceStatus);
    }

    private String determineAttendanceStatus(long elapsedMs) {
        long elapsedMinutes = elapsedMs / (60 * 1000);

        if (elapsedMinutes <= gracePeriod) {
            return "PRESENT";
        } else if (elapsedMinutes <= sessionDuration) {
            return "PRESENT_LATE";
        } else {
            return "SESSION_ENDED";
        }
    }

    private void uploadAttendanceWithSession(File imageFile, String status) {
        // 🌐 NETWORK: Using IP from ApiClient configuration
        ApiService api = ApiClient.getClient().create(ApiService.class);

        RequestBody sessionIdBody = RequestBody.create(MediaType.parse("text/plain"), sessionId);
        RequestBody statusBody = RequestBody.create(MediaType.parse("text/plain"), status);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), reqFile);

        Call<AttendanceResponse> call = api.recordAttendance(sessionIdBody, statusBody, body);
        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceResponse attendanceResponse = response.body();
                    if (attendanceResponse.isSuccess()) {
                        // Show success feedback briefly
                        runOnUiThread(() -> {
                            Toast.makeText(AttendanceSessionActivity.this,
                                    attendanceResponse.getStudentName() + " checked in",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                // Note: We don't show error messages for failed recognitions
                // as this runs continuously in background
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Log.e("AttendanceSession", "Failed to upload attendance", t);
            }
        });
    }

    private void startRealTimeUpdates() {
        // Poll server every 5 seconds for attendance count updates
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchSessionStats();
            }
        }, 0, 5000);
    }

    private void fetchSessionStats() {
        if (sessionId == null) return;

        // 🌐 NETWORK: Using IP from ApiClient configuration
        ApiService api = ApiClient.getClient().create(ApiService.class);

        Call<SessionStatsResponse> call = api.getSessionStats(sessionId);
        call.enqueue(new Callback<SessionStatsResponse>() {
            @Override
            public void onResponse(Call<SessionStatsResponse> call, Response<SessionStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionStatsResponse stats = response.body();

                    mainHandler.post(() -> {
                        // Update attendance count
                        String countText = String.format("%d / %d Present",
                                stats.getPresentCount() + stats.getLateCount(),
                                stats.getTotalStudents());
                        attendanceCountText.setText(countText);

                        // Update recent check-ins
                        if (stats.getRecentCheckIns() != null) {
                            recentCheckIns.clear();
                            recentCheckIns.addAll(stats.getRecentCheckIns());
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<SessionStatsResponse> call, Throwable t) {
                Log.e("AttendanceSession", "Failed to fetch session stats", t);
            }
        });
    }

    private void endSessionManually() {
        new AlertDialog.Builder(this)
                .setTitle("End Session")
                .setMessage("Are you sure you want to end this attendance session?")
                .setPositiveButton("Yes", (dialog, which) -> finalizeSessionOnServer())
                .setNegativeButton("No", null)
                .show();
    }

    private void endSessionAutomatically() {
        isSessionActive = false;

        mainHandler.post(() -> {
            Toast.makeText(this, "Session time expired - ending automatically",
                    Toast.LENGTH_LONG).show();
        });

        // Call server to finalize session
        finalizeSessionOnServer();
    }

    private void finalizeSessionOnServer() {
        if (sessionId == null) {
            finish();
            return;
        }

        isSessionActive = false;

        ApiService api = ApiClient.getClient("http://192.168.1.111:8000/").create(ApiService.class);

        Call<SessionResponse> call = api.endAttendanceSession(sessionId);
        call.enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AttendanceSessionActivity.this,
                            "Session ended successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AttendanceSessionActivity.this,
                            "Failed to end session properly", Toast.LENGTH_SHORT).show();
                }

                showSessionSummary();
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Toast.makeText(AttendanceSessionActivity.this,
                        "Network error ending session", Toast.LENGTH_SHORT).show();
                showSessionSummary();
            }
        });
    }

    private void showSessionSummary() {
        // Show final session statistics
        String summaryMessage = String.format(
                "Session Complete!\n\nFinal Attendance: %s\nSession Duration: %d minutes",
                attendanceCountText.getText().toString(),
                sessionDuration
        );

        new AlertDialog.Builder(this)
                .setTitle("Session Summary")
                .setMessage(summaryMessage)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                setupCamera();
            } else {
                Toast.makeText(this, "Camera permission required for attendance",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up timers
        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
        if (updateTimer != null) {
            updateTimer.cancel();
        }

        // End session if still active
        if (isSessionActive && sessionId != null) {
            finalizeSessionOnServer();
        }
    }
}