package com.example.megamatch;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class rakazLogin extends AppCompatActivity {

    private EditText schoolIdInput, schoolNameInput, usernameInput, passwordInput;
    private final Handler handler = new Handler();
    private Runnable schoolNameUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rakaz_login);

        schoolsDB.loadSchoolsFromCSV(this);

        // Debug: Print school count
        Log.d("SchoolDB", "Total schools loaded: " + schoolsDB.getTotalSchoolsCount());

        // Debug: Manually test if a school exists
        schoolsDB.School testSchool = schoolsDB.getSchoolById(770826);
        if (testSchool != null) {
            Log.d("SchoolDB", "School Found: " + testSchool.getSchoolName());
        } else {
            Log.e("SchoolDB", "School ID 770826 NOT FOUND!");
        }

        schoolIdInput = findViewById(R.id.schoolIDInput);
        schoolNameInput = findViewById(R.id.schoolNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(schoolNameUpdater);
    }

    public void registerRakaz(View view) {
        Toast.makeText(this, "Registration button clicked", Toast.LENGTH_SHORT).show();
    }

    public void moveToRakazRegister(View view) {
        Intent i1 = new Intent(this, rakazRegister.class);
        startActivity(i1);
    }
}
