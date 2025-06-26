package com.example.faceit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class CourseSelectionActivity extends AppCompatActivity implements CourseAdapter.OnCourseClickListener {

    private TextView headerText, teacherNameText, statusText;
    private RecyclerView coursesRecyclerView;
    private MaterialButton refreshButton, backButton;

    private CourseAdapter courseAdapter;
    private List<Course> coursesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_selection);

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load teacher info
        loadTeacherInfo();

        // Load courses from Django backend
        loadTeacherCourses();

        // Setup button listeners
        refreshButton.setOnClickListener(v -> loadTeacherCourses());
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        headerText = findViewById(R.id.header_text);
        teacherNameText = findViewById(R.id.teacher_name_text);
        statusText = findViewById(R.id.status_text);
        coursesRecyclerView = findViewById(R.id.courses_recycler_view);
        refreshButton = findViewById(R.id.refresh_button);
        backButton = findViewById(R.id.back_button);
    }

    private void setupRecyclerView() {
        courseAdapter = new CourseAdapter(coursesList, this);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        coursesRecyclerView.setAdapter(courseAdapter);
    }

    private void loadTeacherInfo() {
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        String teacherName = prefs.getString("teacher_name", "Unknown Teacher");
        teacherNameText.setText("👨‍🏫 " + teacherName);
    }

    private void loadTeacherCourses() {
        statusText.setText("🔄 Loading your courses...");
        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");

        // Get teacher token for authentication
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        String token = prefs.getString("teacher_token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "❌ Authentication error. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Call Django courses API with teacher authentication
        ApiService api = ApiClient.getClient().create(ApiService.class);
        String authHeader = "Bearer " + token;

        Call<CoursesListResponse> call = api.getTeacherCourses(authHeader, true); // active_only = true
        call.enqueue(new Callback<CoursesListResponse>() {
            @Override
            public void onResponse(Call<CoursesListResponse> call, Response<CoursesListResponse> response) {
                // Reset button state
                refreshButton.setEnabled(true);
                refreshButton.setText("Refresh");

                if (response.isSuccessful() && response.body() != null) {
                    CoursesListResponse coursesResponse = response.body();

                    // ✅ FIX: Add null check for results
                    List<Course> courses = coursesResponse.getResults();
                    if (courses == null) {
                        courses = new ArrayList<>();
                    }

                    Log.d("CourseSelection", "✅ Loaded " + courses.size() + " courses");

                    if (courses.isEmpty()) {
                        statusText.setText("📚 No courses assigned to you currently.");
                        coursesList.clear();
                    } else {
                        statusText.setText("📚 Select a course to start attendance:");
                        coursesList.clear();
                        coursesList.addAll(courses);
                    }

                    courseAdapter.notifyDataSetChanged();

                } else {
                    Log.e("CourseSelection", "❌ Failed to load courses. Response code: " + response.code());

                    String errorMsg = "❌ Failed to load courses.";
                    if (response.code() == 401) {
                        errorMsg += " Please login again.";
                    } else if (response.code() == 403) {
                        errorMsg += " Access denied.";
                    } else if (response.code() == 404) {
                        errorMsg += " Courses endpoint not found.";
                    } else if (response.code() >= 500) {
                        errorMsg += " Server error.";
                    }

                    statusText.setText(errorMsg);
                    Toast.makeText(CourseSelectionActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                    // ✅ FIX: Clear courses list on error
                    coursesList.clear();
                    courseAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CoursesListResponse> call, Throwable t) {
                // Reset button state
                refreshButton.setEnabled(true);
                refreshButton.setText("Refresh");

                Log.e("CourseSelection", "❌ Network error loading courses", t);

                statusText.setText("❌ Network error. Check connection.");
                Toast.makeText(CourseSelectionActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();

                // ✅ FIX: Clear courses list on network error
                coursesList.clear();
                courseAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCourseClick(Course course) {
        // ✅ FIX: Add null check for course
        if (course == null) {
            Toast.makeText(this, "❌ Invalid course selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CourseSelection", "✅ Selected course: " + course.getSafeDisplayName());

        // ✅ FIX: Use safe methods and add validation
        String courseId = String.valueOf(course.getId());
        String courseName = course.getSafeDisplayName();
        String courseCode = (course.getCourse_code() != null) ? course.getCourse_code() : "Unknown";

        // Validate course ID
        if (course.getId() <= 0) {
            Toast.makeText(this, "❌ Invalid course ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start attendance activity with selected course
        Intent intent = new Intent(this, BasicAttendanceActivity.class);
        intent.putExtra("course_id", courseId);
        intent.putExtra("course_name", courseName);
        intent.putExtra("course_code", courseCode);
        intent.putExtra("credits", course.getCredits());

        startActivity(intent);

        // Show feedback
        Toast.makeText(this,
                "Starting attendance for " + courseName,
                Toast.LENGTH_SHORT).show();
    }

    // ✅ ADD: Lifecycle method to handle activity resume
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh courses when returning to this activity
        Log.d("CourseSelection", "Activity resumed - refreshing courses");
    }

    // ✅ ADD: Method to handle authentication errors
    private void handleAuthenticationError() {
        // Clear stored tokens
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("teacher_token");
        editor.remove("teacher_refresh_token");
        editor.apply();

        // Go back to login
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
    }
}