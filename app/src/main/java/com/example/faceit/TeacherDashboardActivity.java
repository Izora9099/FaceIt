package com.example.faceit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView teacherNameText, currentTimeText, statsText;
    private MaterialButton startSessionButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize views
        teacherNameText = findViewById(R.id.teacher_name_text);
        currentTimeText = findViewById(R.id.current_time_text);
        statsText = findViewById(R.id.stats_text);
        startSessionButton = findViewById(R.id.start_session_button);
        logoutButton = findViewById(R.id.logout_button);

        // Load teacher information
        loadTeacherInfo();

        // Load dashboard stats from Django
        loadDashboardStats();

        // Set up button listeners
        startSessionButton.setOnClickListener(v -> startCourseSelection());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadTeacherInfo() {
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        String teacherName = prefs.getString("teacher_name", "Unknown Teacher");

        teacherNameText.setText("Welcome, " + teacherName);

        // Update current time
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        currentTimeText.setText(sdf.format(new java.util.Date()));
    }

    private void loadDashboardStats() {
        // Get teacher token for authentication
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        String token = prefs.getString("teacher_token", "");

        if (token.isEmpty()) {
            statsText.setText("📊 Stats unavailable (Not authenticated)");
            return;
        }

        // Call Django dashboard stats API
        ApiService api = ApiClient.getClient().create(ApiService.class);
        String authHeader = "Bearer " + token;

        Call<DashboardStatsResponse> call = api.getDashboardStats(authHeader);
        call.enqueue(new Callback<DashboardStatsResponse>() {
            @Override
            public void onResponse(Call<DashboardStatsResponse> call, Response<DashboardStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardStatsResponse stats = response.body();

                    // Display teacher-specific stats if available
                    String statsDisplay = "";
                    if (stats.getMy_courses() > 0) {
                        statsDisplay = String.format(
                                "📚 My Courses: %d\n👥 My Students: %d\n✅ Today's Attendance: %d",
                                stats.getMy_courses(),
                                stats.getMy_students(),
                                stats.getMy_attendance_today()
                        );
                    } else {
                        // Fallback to general stats
                        statsDisplay = String.format(
                                "📚 Total Courses: %d\n👥 Total Students: %d\n✅ Today's Attendance: %d",
                                stats.getTotal_courses(),
                                stats.getTotal_students(),
                                stats.getTotal_attendance_today()
                        );
                    }

                    statsText.setText(statsDisplay);

                    Log.d("TeacherDashboard", "✅ Dashboard stats loaded successfully");

                } else {
                    statsText.setText("📊 Stats unavailable");
                    Log.e("TeacherDashboard", "❌ Failed to load stats. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DashboardStatsResponse> call, Throwable t) {
                statsText.setText("📊 Stats unavailable (Network Error)");
                Log.e("TeacherDashboard", "❌ Network error loading stats", t);
            }
        });
    }

    private void startCourseSelection() {
        // ✅ NEW: Open real course selection instead of hardcoded demo
        Intent intent = new Intent(this, CourseSelectionActivity.class);
        startActivity(intent);

        Log.d("TeacherDashboard", "🎯 Opening course selection");
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