package com.example.megamatch;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class talmidLogin extends AppCompatActivity {

    private EditText schoolIdInput, schoolNameInput, passwordInput;
    private final Handler handler = new Handler();
    private Runnable schoolNameUpdater;
    private Button talmidLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talmid_login);

        schoolsDB.loadSchoolsFromCSV(this);
//        HashMap<Integer, schoolsDB.School> schoolsMap = schoolsDB.getSchoolsMap();

        Log.d("SchoolDB", "Total schools loaded: " + schoolsDB.getTotalSchoolsCount()); // Shows the total schools loaded from the database

        schoolIdInput = findViewById(R.id.schoolIdInput);
        schoolNameInput = findViewById(R.id.schoolNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        talmidLoginButton = findViewById(R.id.talmidLoginButton);

        // Make schoolNameInput non-editable
        schoolNameInput.setFocusableInTouchMode(false);

        startSchoolIdChecker();

        talmidLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String schoolId = schoolIdInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (!schoolId.isEmpty() && !password.isEmpty()) {
                    if(schoolId.length() == 6 && schoolId.matches("\\d+"))
                    {
                        Toast.makeText(talmidLogin.this, "School ID: " + schoolId + "\nPassword: " + password, Toast.LENGTH_LONG).show();
                    }

                    else
                    {
                        Toast.makeText(talmidLogin.this, "נא להכניס מספר סימול ביהס קיים באורך 6 ספרות", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(talmidLogin.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
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
                        schoolNameInput.setHint("שם ביהס: " + schoolName);
                    } else {
                        schoolNameInput.setHint("שם ביהס: ");
                    }
                } else {
                    schoolNameInput.setHint("שם ביהס: ");
                }
                handler.postDelayed(this, 3000); // Run again after 3 seconds
            }
        };
        handler.post(schoolNameUpdater);
    }

}
