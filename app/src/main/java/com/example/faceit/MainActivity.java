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
        setContentView(R.layout.activity_main); // Make sure this matches your layout file name

        registerFaceButton = findViewById(R.id.register_face_button); // Add an id to this button in XML
        takeAttendanceButton = findViewById(R.id.take_attendance_button); // Add an id to this button in XML

        registerFaceButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterStudent1.class));
        });

        takeAttendanceButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ScanFace.class));
        });
    }
}
