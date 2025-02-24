package com.example.megamatch;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
                "משפטים", "תיאטרון", "משפט עברי"
        };

        // Initialize the Spinner
        Spinner megamaSpinner = findViewById(R.id.megamaSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, megamaList);
        megamaSpinner.setAdapter(adapter);
    }
}
