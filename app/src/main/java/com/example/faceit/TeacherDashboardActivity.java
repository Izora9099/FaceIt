package com.example.faceit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView teacherNameText, currentTimeText;
    private MaterialButton startSessionButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize views
        teacherNameText = findViewById(R.id.teacher_name_text);
        currentTimeText = findViewById(R.id.current_time_text);
        startSessionButton = findViewById(R.id.start_session_button);
        logoutButton = findViewById(R.id.logout_button);

        // Load teacher information
        loadTeacherInfo();

        // Set up button listeners
        startSessionButton.setOnClickListener(v -> startDemoSession());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadTeacherInfo() {
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        String teacherName = prefs.getString("teacher_name", "Unknown Teacher");

        teacherNameText.setText("Welcome, " + teacherName);

        // Update current time (you can make this real-time later)
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        currentTimeText.setText(sdf.format(new java.util.Date()));
    }

    private void startDemoSession() {
        // For now, start a demo session
        // Later you'll implement proper course selection

        Intent intent = new Intent(this, AttendanceSessionActivity.class);
        intent.putExtra("course_id", "demo_course_001");
        intent.putExtra("course_name", "Demo Course - Computer Science");
        intent.putExtra("session_duration", 120); // 2 hours

        startActivity(intent);
    }

    private void logout() {
        // Clear saved teacher data
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Go back to login
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}