package com.project.megamatch;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class rakazRegister extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private AutoCompleteTextView megamaSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.rakaz_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rakazRegister), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // רשימת המגמות
        String[] megamaList = {
                "מחשבים", "כימיה", "ביולוגיה", "פיסיקה", "ערבית",
                "מכטרוניקה", "רובוטיקה", "טלוויזיה", "אומנות",
                "משפטים", "תיאטרון", "מחול", "משפט עברי", "אלקטרוניקה"
        };

        // קישור לרכיבי UI
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        megamaSpinner = findViewById(R.id.megamaSpinner);

        // הגדרת אדפטר למגמות
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, megamaList);
        megamaSpinner.setAdapter(adapter);
    }

    // פונקציה להרשמת רכז
    public void registerRakaz(View view) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String megama = megamaSpinner.getText().toString();

        if (!username.isEmpty() && !password.isEmpty()) {
            String message = "Username: " + username + "\nMegama: " + megama;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
        }
    }
}
