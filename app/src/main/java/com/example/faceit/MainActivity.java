package com.example.faceit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    TextInputEditText usernameInput, passwordInput;
    MaterialButton loginButton;

    // ====================================================================
    // 🌐 NETWORK CONFIGURATION - UPDATE WHEN CHANGING WIFI NETWORKS
    // ====================================================================
    //
    // CURRENT COMPUTER IP: 172.30.62.139
    //
    // ⚠️ IMPORTANT: When you change WiFi networks, you MUST:
    //
    // 1. Find your computer's new IP address:
    //    Windows: Open CMD and run "ipconfig"
    //    Mac/Linux: Open Terminal and run "ifconfig" or "ip addr"
    //
    // 2. Look for the IP address under your WiFi adapter
    //
    // 3. Update the API_BASE_URL below with the new IP
    //    Format: "http://YOUR_NEW_IP:8000/"
    //
    // 4. Make sure Django is running with: python manage.py runserver 0.0.0.0:8000
    //
    // 5. Test connection by visiting http://YOUR_NEW_IP:8000/admin/ in your phone's browser
    //
    // ====================================================================

    private static final String API_BASE_URL = "http://172.30.62.139:8000/"; // ⬅️ UPDATE THIS IP WHEN CHANGING NETWORKS

    // Alternative URLs for different network scenarios (uncomment as needed):
    // private static final String API_BASE_URL = "http://192.168.1.XXX:8000/";   // Common home WiFi range
    // private static final String API_BASE_URL = "http://192.168.0.XXX:8000/";   // Common home WiFi range
    // private static final String API_BASE_URL = "http://10.0.0.XXX:8000/";      // Corporate/office WiFi range
    // private static final String API_BASE_URL = "http://10.0.2.2:8000/";        // Android Emulator only
    // private static final String API_BASE_URL = "http://192.168.43.XXX:8000/";  // Phone hotspot range

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        // Initialize UI elements
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> authenticateTeacher());

        // Check if teacher is already logged in
        checkExistingLogin();

        // Log current configuration for debugging
        Log.d("MainActivity", "🌐 Configured to connect to: " + API_BASE_URL);
        Log.d("MainActivity", "💡 If connection fails, check if computer IP has changed!");
    }

    private void checkExistingLogin() {
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        String teacherId = prefs.getString("teacher_id", "");
        String teacherToken = prefs.getString("teacher_token", "");

        if (!teacherId.isEmpty() && !teacherToken.isEmpty()) {
            // Teacher already logged in, go to dashboard
            goToTeacherDashboard();
        }
    }

    private void authenticateTeacher() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        Log.d("MainActivity", "🔄 Attempting to connect to: " + API_BASE_URL);
        Log.d("MainActivity", "👤 Username: " + username);

        // Call API to authenticate teacher
        ApiService api = ApiClient.getClient(API_BASE_URL).create(ApiService.class);

        Call<TeacherAuthResponse> call = api.authenticateTeacher(username, password);
        call.enqueue(new Callback<TeacherAuthResponse>() {
            @Override
            public void onResponse(Call<TeacherAuthResponse> call, Response<TeacherAuthResponse> response) {
                // Reset button state
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                Log.d("MainActivity", "📡 Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    TeacherAuthResponse authResponse = response.body();

                    // For JWT response, we get 'access' token but need to extract user info
                    if (authResponse.getAccess() != null && !authResponse.getAccess().isEmpty()) {

                        // Save JWT tokens
                        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putString("teacher_token", authResponse.getAccess());
                        editor.putString("teacher_refresh_token", authResponse.getRefresh());
                        editor.putString("teacher_id", username); // Use username as ID for now
                        editor.putString("teacher_name", username); // Use username as name for now
                        editor.apply();

                        // Show success message
                        Toast.makeText(MainActivity.this,
                                "✅ Welcome, " + username,
                                Toast.LENGTH_SHORT).show();

                        Log.d("MainActivity", "✅ JWT Authentication successful for: " + username);

                        // Navigate to Teacher Dashboard
                        goToTeacherDashboard();

                    } else {
                        Toast.makeText(MainActivity.this, "❌ Invalid response from server", Toast.LENGTH_LONG).show();
                    }

                } else if (response.code() == 404) {
                    // Endpoint not found - likely Django URL issue
                    String errorMsg = "❌ Endpoint not found!\n\n" +
                            "Make sure Django has the URL:\n" +
                            "auth/teacher/login/\n\n" +
                            "Check your Django urls.py file.";

                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "❌ 404 Error - Check Django URLs");

                } else {
                    // Authentication failed or other error
                    String errorMessage = "❌ Authentication failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error parsing response", e);
                    }

                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "❌ Auth failed: " + errorMessage);

                    // Clear password field
                    passwordInput.setText("");
                    passwordInput.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<TeacherAuthResponse> call, Throwable t) {
                // Reset button state
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                Log.e("MainActivity", "🚫 Network error connecting to: " + API_BASE_URL, t);

                // Create detailed error message with troubleshooting steps
                String errorMsg = "🚫 Cannot connect to Django server!\n\n" +
                        "📍 Current IP: " + API_BASE_URL + "\n\n" +
                        "🔧 Troubleshooting:\n\n" +
                        "1️⃣ Check if Django is running:\n" +
                        "   python manage.py runserver 0.0.0.0:8000\n\n" +
                        "2️⃣ If WiFi changed, update IP in code:\n" +
                        "   - Run 'ipconfig' (Windows) or 'ifconfig' (Mac/Linux)\n" +
                        "   - Update API_BASE_URL in MainActivity.java\n\n" +
                        "3️⃣ Test in browser: " + API_BASE_URL + "admin/\n\n" +
                        "4️⃣ Check same WiFi network\n\n" +
                        "Error: " + t.getMessage();

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Connection Failed")
                        .setMessage(errorMsg)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void saveTeacherDetails(TeacherAuthResponse authResponse) {
        SharedPreferences prefs = getSharedPreferences("faceit_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("teacher_id", authResponse.getTeacherId());
        editor.putString("teacher_name", authResponse.getName());
        editor.putString("teacher_token", authResponse.getToken());

        // Save assigned courses as a comma-separated string
        if (authResponse.getAssignedCourses() != null) {
            String coursesString = String.join(",", authResponse.getAssignedCourses());
            editor.putString("assigned_courses", coursesString);
        }

        editor.apply();

        Log.d("MainActivity", "💾 Teacher details saved to SharedPreferences");
    }

    private void goToTeacherDashboard() {
        Intent intent = new Intent(MainActivity.this, TeacherDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Log.d("MainActivity", "🎯 Navigating to Teacher Dashboard");
    }
}