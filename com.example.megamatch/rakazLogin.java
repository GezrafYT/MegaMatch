package com.example.megamatch;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class rakazLogin extends AppCompatActivity {

    private EditText schoolIdInput, schoolNameInput, usernameInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rakaz_login);

        schoolIdInput = findViewById(R.id.schoolIDInput);
        schoolNameInput = findViewById(R.id.schoolNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
    }

    public void registerRakaz(View view) {
        Toast.makeText(this, "Registration button clicked", Toast.LENGTH_SHORT).show();
    }

    public void moveToRakazRegister(View view)
    {
        Intent i1 = new Intent(this, rakazRegister.class);
        startActivity(i1);
    }
}
