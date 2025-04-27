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

public class rakazLogin extends AppCompatActivity {

    private EditText schoolIdInput, schoolNameInput, usernameInput, passwordInput;
    private Button rakazLoginButton;
    private Button noAccountButton;
    private FirebaseFirestore fireDB;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.rakaz_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rakazLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load schools data
        schoolsDB.loadSchoolsFromCSV(this);
        Log.d("SchoolDB", "Total schools loaded: " + schoolsDB.getTotalSchoolsCount());

        schoolIdInput = findViewById(R.id.schoolIDInput);
        schoolNameInput = findViewById(R.id.schoolNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        rakazLoginButton = findViewById(R.id.rakazLoginButton);
        noAccountButton = findViewById(R.id.noAccountButton);

        fireDB = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("MegaMatchPrefs", MODE_PRIVATE);

        // Make schoolNameInput non-editable
        schoolNameInput.setFocusableInTouchMode(false);

        // Check if Rakaz is already logged in (saved session)
        checkIfAlreadyLoggedIn();

        // Automatically fill school name based on ID input
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

        rakazLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String schoolId = schoolIdInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String username = usernameInput.getText().toString().trim();

                if (!schoolId.isEmpty() && !password.isEmpty() && !username.isEmpty()) {
                    if (schoolId.length() == 6 && schoolId.matches("\\d+")) {
                        fireDB.collection("schools").document(schoolId)
                                .collection("rakazim").document(username)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String storedPassword = documentSnapshot.getString("password");
                                        if (storedPassword != null && storedPassword.equals(password)) {
                                            // Save session and redirect
                                            saveRakazSession(schoolId, username);
                                            goToNextScreen();
                                        } else {
                                            Toast.makeText(rakazLogin.this, "סיסמה שגויה", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(rakazLogin.this, "רכז לא נמצא במערכת", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(rakazLogin.this, "שגיאה בגישה לנתונים", Toast.LENGTH_SHORT).show();
                                    Log.e("FirestoreError", "Error checking username", e);
                                });
                    } else {
                        Toast.makeText(rakazLogin.this, "נא להכניס מספר סימול ביהס קיים באורך 6 ספרות", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(rakazLogin.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Check if Rakaz is already logged in
    private void checkIfAlreadyLoggedIn() {
        if (sharedPreferences.contains("loggedInUsername")) {
            Log.d("Auth", "Rakaz session found, auto-login");
            goToNextScreen();
        }
    }


    private void saveRakazSession(String schoolId, String username) {
        // Save session data in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("loggedInSchoolId", schoolId);
        editor.putString("loggedInUsername", username);
        editor.apply();
    }

    private void goToNextScreen() {
        Intent intent = new Intent(this, rakazPage.class);
        startActivity(intent);
        finish();
    }

    public void moveToRakazRegister(View view) {
        Intent i1 = new Intent(this, rakazRegister.class);
        startActivity(i1);
    }
}
