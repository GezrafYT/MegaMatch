package com.example.megamatch;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class rakazRegister extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Spinner megamaSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rakaz_register);

        // Define the list of Megamas
        String[] megamaList = {
                "מחשבים", "כימיה", "ביולוגיה", "פיסיקה", "ערבית",
                "מכטרוניקה", "רובוטיקה", "טלוויזיה", "אומנות",
                "משפטים", "תיאטרון", "מחול", "משפט עברי", "אלקטרוניקה"
        };

        // Initialize UI elements
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        megamaSpinner = findViewById(R.id.megamaSpinner);

        // Set up the spinner with the Megama list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, megamaList);
        megamaSpinner.setAdapter(adapter);
    }

    public void registerRakaz(View view) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String megama = megamaSpinner.getSelectedItem().toString();

        if (!username.isEmpty() && !password.isEmpty()) {
            String message = "פרטי הרכז\nUsername: " + username + "\nPassword: " + password + "\nMegama: " + megama;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
        }
    }
}
