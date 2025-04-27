package com.project.megamatch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;


public class talmidLogin extends AppCompatActivity {

    private EditText schoolIdInput, schoolNameInput, talmidIDInput, passwordInput;
    private Button talmidLoginButton;
    private FirebaseFirestore fireDB;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.talmid_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.talmidLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // טעינת בתי הספר מקובץ
        schoolsDB.loadSchoolsFromCSV(this);
        Log.d("SchoolDB", "Total schools loaded: " + schoolsDB.getTotalSchoolsCount());

        // אתחול רכיבי ממשק
        schoolIdInput = findViewById(R.id.schoolIdInput);
        schoolNameInput = findViewById(R.id.schoolNameInput);
        talmidIDInput = findViewById(R.id.talmidIDInput);
        passwordInput = findViewById(R.id.passwordInput);
        talmidLoginButton = findViewById(R.id.talmidLoginButton);

        fireDB = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("MegaMatchPrefs", MODE_PRIVATE);

        // שדה שם בית ספר לקריאה בלבד
        schoolNameInput.setFocusableInTouchMode(false);

        // בדיקה אם המשתמש כבר מחובר
        checkIfAlreadyLoggedIn();

        // עדכון אוטומטי של שם בית הספר לפי הסימול
        schoolIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String schoolId = s.toString().trim();
                if (schoolId.length() == 6 && schoolId.matches("\\d+")) {
                    int id = Integer.parseInt(schoolId);
                    schoolsDB.School school = schoolsDB.getSchoolById(id);
                    if (school != null) {
                        schoolNameInput.setText(school.getSchoolName());
                    } else {
                        schoolNameInput.setText("");
                    }
                } else {
                    schoolNameInput.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // לחיצה על כפתור התחברות
        talmidLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String schoolId = schoolIdInput.getText().toString().trim();
                String talmidId = talmidIDInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (!schoolId.isEmpty() && !talmidId.isEmpty() && !password.isEmpty()) {
                    if (schoolId.length() == 6 && schoolId.matches("\\d+")) {
                        if (talmidId.length() == 9) {
                            // בדיקת פרטי התחברות בפיירבייס
                            fireDB.collection("schools").document(schoolId)
                                    .collection("talmidim").document(talmidId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String storedPassword = documentSnapshot.getString("password");
                                            if (storedPassword != null && storedPassword.equals(password)) {
                                                // שמירת סשן והפניה לדף הבא
                                                saveTalmidSession(schoolId, talmidId);
                                                goToNextScreen();
                                            } else {
                                                Toast.makeText(talmidLogin.this, "סיסמה שגויה", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(talmidLogin.this, "תלמיד לא נמצא במערכת", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(talmidLogin.this, "שגיאה בגישה לנתונים", Toast.LENGTH_SHORT).show();
                                        Log.e("FirestoreError", "Error checking talmidId", e);
                                    });

                        } else {
                            Toast.makeText(talmidLogin.this, "נא להכניס תעודה מזהה קיימת באורך 9 ספרות", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(talmidLogin.this, "נא להכניס מספר סימול ביהס קיים באורך 6 ספרות", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(talmidLogin.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // בדיקה אם קיים חיבור קודם
    private void checkIfAlreadyLoggedIn() {
        if (sharedPreferences.contains("loggedInTalmidId")) {
            Log.d("Auth", "Talmid session found, auto-login");
            goToNextScreen();
        }
    }

    // שמירת פרטי התחברות
    private void saveTalmidSession(String schoolId, String talmidId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("loggedInSchoolId", schoolId);
        editor.putString("loggedInTalmidId", talmidId);
        editor.apply();
    }

    // מעבר למסך הבא
    private void goToNextScreen() {
        Intent intent = new Intent(this, talmidPage.class);
        startActivity(intent);
        finish();
    }
}
