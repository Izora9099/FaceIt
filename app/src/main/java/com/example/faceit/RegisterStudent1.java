package com.example.faceit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterStudent1 extends AppCompatActivity {

    TextInputEditText nameInput, matricInput;
    MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student1); // Your layout filename

        nameInput = findViewById(R.id.name_input);    // Assign these ids in your XML
        matricInput = findViewById(R.id.matric_input);
        nextButton = findViewById(R.id.next_button);

        nextButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String matric = matricInput.getText().toString().trim();

            SharedPreferences prefs = getSharedPreferences("faceit_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("student_name", name);
            editor.putString("student_matric", matric);
            editor.apply();

            startActivity(new Intent(RegisterStudent1.this, ScanFace.class));
        });
    }
}
