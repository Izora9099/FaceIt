package com.example.faceit;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScanFace extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private PreviewView previewView;
    private Button finishButton;

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
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_face);

        SharedPreferences prefs = getSharedPreferences("faceit_prefs", Context.MODE_PRIVATE);
        String name = prefs.getString("student_name", "Unknown");
        String matric = prefs.getString("student_matric", "N/A");

        previewView = findViewById(R.id.previewView);
        finishButton = findViewById(R.id.finishButton);

        requestAllPermissions();

        finishButton.setOnClickListener(v -> {
            // TODO: Add logic to handle scan result submission here
            Toast.makeText(ScanFace.this, "Scan finished.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void requestAllPermissions() {
        List<String> allPermissions = new ArrayList<>();
        for (String perm : basePermissions) allPermissions.add(perm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String perm : mediaPermissions13Plus) allPermissions.add(perm);
        } else {
            for (String perm : storagePermissionsBelow13) allPermissions.add(perm);
        }

        List<String> toRequest = new ArrayList<>();
        for (String perm : allPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(perm);
            }
        }

        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    toRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
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
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(
                        (LifecycleOwner) this,
                        cameraSelector,
                        preview);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
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
                startCamera();
            } else {
                Toast.makeText(this, "All permissions are required to continue.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
