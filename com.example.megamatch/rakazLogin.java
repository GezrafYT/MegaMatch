package com.example.megamatch;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class rakazLogin extends AppCompatActivity {

    private EditText schoolIdInput, schoolNameInput, usernameInput, passwordInput;
    private final Handler handler = new Handler();
    private Runnable schoolNameUpdater;

    private Button rakazLoginButton = findViewById(R.id.rakazLoginButton);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rakaz_login);

        schoolsDB.loadSchoolsFromCSV(this);
//        HashMap<Integer, schoolsDB.School> schoolsMap = schoolsDB.getSchoolsMap();

        Log.d("SchoolDB", "Total schools loaded: " + schoolsDB.getTotalSchoolsCount()); // Shows the total schools loaded from the database

        schoolIdInput = findViewById(R.id.schoolIDInput);
        schoolNameInput = findViewById(R.id.schoolNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);

        // Make schoolNameInput non-editable
        schoolNameInput.setFocusableInTouchMode(false);

        startSchoolIdChecker();

        // Handle login button click
        rakazLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String schoolId = schoolIdInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (!schoolId.isEmpty() && !password.isEmpty()) {
                    if (schoolId.length() == 6 && schoolId.matches("\\d+")) {
                        Toast.makeText(rakazLogin.this, "School ID: " + schoolId + "\nPassword: " + password, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(rakazLogin.this, "נא להכניס מספר סימול ביהס קיים באורך 6 ספרות", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(rakazLogin.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void startSchoolIdChecker() {
        schoolNameUpdater = new Runnable() {
            @Override
            public void run() {
                String schoolId = schoolIdInput.getText().toString();
                if (schoolId.length() == 6 && schoolId.matches("\\d+")) { // אם מספר סימול ביהס בעל אורך של 6 ספרות
                    int id = Integer.parseInt(schoolId);
                    schoolsDB.School school = schoolsDB.getSchoolsMap().get(id);
                    if (school != null) {
                        String schoolName = school.getSchoolName();
                        schoolNameInput.setHint(schoolName);
                    } else {
                        schoolNameInput.setHint("");
                    }
                } else {
                    schoolNameInput.setHint("");
                }
                handler.postDelayed(this, 2000); // Run again after 2 seconds
            }
        };
        handler.post(schoolNameUpdater);
    }


    public void moveToRakazRegister(View view) {
        Intent i1 = new Intent(this, rakazRegister.class);
        startActivity(i1);
    }
}
