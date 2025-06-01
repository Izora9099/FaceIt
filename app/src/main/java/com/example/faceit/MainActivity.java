package com.example.faceit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {



    MaterialButton registerFaceButton, takeAttendanceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerFaceButton = findViewById(R.id.register_face_button);
        takeAttendanceButton = findViewById(R.id.take_attendance_button);

        registerFaceButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterStudent1.class));
        });

        takeAttendanceButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ScanFace.class));
        });
    }
}
