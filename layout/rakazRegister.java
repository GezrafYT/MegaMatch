package com.example.megamatch;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class rakazRegister extends AppCompatActivity {

    private EditText schoolIdInput, passwordInput;
    private Button talmidLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rakaz_register);

//        schoolIdInput = findViewById(R.id.schoolIdInput);
//        passwordInput = findViewById(R.id.passwordInput);
//        talmidLoginButton = findViewById(R.id.talmidLoginButton);
//
//        talmidLoginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String schoolId = schoolIdInput.getText().toString().trim();
//                String password = passwordInput.getText().toString().trim();
//
//                if (!schoolId.isEmpty() && !password.isEmpty()) {
//                    Toast.makeText(rakazRegister.this, "School ID: " + schoolId + "\nPassword: " + password, Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(rakazRegister.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }
}