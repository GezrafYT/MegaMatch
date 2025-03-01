package com.example.megamatch;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class rakazRegister extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rakaz_register);

        // Define the list of Megamas
        String[] megamaList = {
                "מחשבים", "כימיה", "ביולוגיה", "פיסיקה", "ערבית",
                "מכטרוניקה", "רובוטיקה", "טלוויזיה", "אומנות",
                "משפטים", "תיאטרון", "מחול", "משפט עברי", "אלטקטרוניקה"
        };

        // Initialize the Spinner
        Spinner megamaSpinner = findViewById(R.id.megamaSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, megamaList);
        megamaSpinner.setAdapter(adapter);
    }

    public void registerRakaz(View view) {
        Toast.makeText(this, "Registration button clicked", Toast.LENGTH_SHORT).show();
    }
}
