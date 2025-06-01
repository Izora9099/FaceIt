package com.example.faceit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterStudent1 extends AppCompatActivity {

    TextInputEditText nameInput, matricInput;
    MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student1); // Ensure correct layout name

        // Link UI elements
        nameInput = findViewById(R.id.name_input);
        matricInput = findViewById(R.id.matric_input);
        nextButton = findViewById(R.id.next_button);

        nextButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String matric = matricInput.getText().toString().trim();

            if (name.isEmpty() || matric.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("faceit_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("student_name", name);
            editor.putString("student_matric", matric);
            editor.apply();

            // Proceed to face scanning
            Intent intent = new Intent(RegisterStudent1.this, ScanFace.class);
            startActivity(intent);
        });
    }
}
