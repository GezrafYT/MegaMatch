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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class rakazLogin extends AppCompatActivity {

    private AutoCompleteTextView schoolAutocomplete;
    private EditText usernameInput, passwordInput;
    private Button rakazLoginButton;
    private Button noAccountButton;
    private FirebaseFirestore fireDB;
    private SharedPreferences sharedPreferences;
    private List<schoolsDB.School> allSchools;
    private SchoolAdapter schoolAdapter;
    private schoolsDB.School selectedSchool;

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

        schoolAutocomplete = findViewById(R.id.schoolAutocomplete);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        rakazLoginButton = findViewById(R.id.rakazLoginButton);
        noAccountButton = findViewById(R.id.noAccountButton);

        fireDB = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("MegaMatchPrefs", MODE_PRIVATE);

        // Check if Rakaz is already logged in (saved session)
        checkIfAlreadyLoggedIn();

        // Setup school autocomplete
        setupSchoolAutocomplete();
        
        // Setup noAccountButton to redirect to registration
        noAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToRakazRegister(v);
            }
        });

        rakazLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = schoolAutocomplete.getText().toString().trim();
                
                // Double-check if text matches a school name but selectedSchool isn't set
                if (selectedSchool == null && !currentText.isEmpty()) {
                    // Try to find by name
                    for (schoolsDB.School school : allSchools) {
                        if (school.getSchoolName().equals(currentText)) {
                            selectedSchool = school;
                            break;
                        }
                    }
                    
                    // Try to find by ID if it's numeric
                    if (selectedSchool == null && currentText.matches("\\d+") && currentText.length() == 6) {
                        int schoolId = Integer.parseInt(currentText);
                        selectedSchool = schoolsDB.getSchoolById(schoolId);
                    }
                }
                
                String password = passwordInput.getText().toString().trim();
                String username = usernameInput.getText().toString().trim();

                if (selectedSchool != null && !password.isEmpty() && !username.isEmpty()) {
                    String schoolId = String.valueOf(selectedSchool.getSchoolId());
                    fireDB.collection("schools").document(schoolId)
                            .collection("rakazim").document(username)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String storedPassword = documentSnapshot.getString("password");
                                    if (storedPassword != null && storedPassword.equals(password)) {
                                        // Save session and redirect
                                        saveRakazSession(schoolId, username);
                                        
                                        // For testing purposes: grant admin privileges to specific account
                                        if (username.equals("admin")) {
                                            grantAdminPrivileges(schoolId, username);
                                        } else {
                                            goToNextScreen();
                                        }
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
                } else if (selectedSchool == null) {
                    Toast.makeText(rakazLogin.this, "נא לבחור בית ספר", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(rakazLogin.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSchoolAutocomplete() {
        // קבלת רשימת בתי הספר מהמסד
        allSchools = schoolsDB.getAllSchools();
        
        // יצירת מתאם מותאם
        schoolAdapter = new SchoolAdapter(this, android.R.layout.simple_dropdown_item_1line, allSchools);
        schoolAutocomplete.setAdapter(schoolAdapter);
        
        // טיפול בבחירת בית ספר מהרשימה
        schoolAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSchool = (schoolsDB.School) parent.getItemAtPosition(position);
                schoolAutocomplete.setText(selectedSchool.getSchoolName());
            }
        });
        
        // טיפול בהקלדה בשדה החיפוש
        schoolAutocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                
                // בדיקה אם הוזן מספר סימול בית ספר
                if (input.matches("\\d+") && input.length() == 6) {
                    int schoolId = Integer.parseInt(input);
                    schoolsDB.School school = schoolsDB.getSchoolById(schoolId);
                    if (school != null) {
                        selectedSchool = school;
                        schoolAutocomplete.setText(selectedSchool.getSchoolName());
                        schoolAutocomplete.dismissDropDown();
                    }
                } else if (!input.equals(selectedSchool != null ? selectedSchool.getSchoolName() : "")) {
                    // Only reset selection if the text doesn't match the current selected school
                    selectedSchool = null;
                    
                    // Check if the exact text matches a school name
                    for (schoolsDB.School school : allSchools) {
                        if (school.getSchoolName().equals(input)) {
                            selectedSchool = school;
                            break;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // מתאם מותאם עם פונקציית סינון מותאמת
    private class SchoolAdapter extends ArrayAdapter<schoolsDB.School> implements Filterable {
        private List<schoolsDB.School> originalList;
        private List<schoolsDB.School> filteredList;

        public SchoolAdapter(rakazLogin context, int resource, List<schoolsDB.School> objects) {
            super(context, resource, objects);
            this.originalList = new ArrayList<>(objects);
            this.filteredList = new ArrayList<>(objects);
        }

        @Override
        public int getCount() {
            return filteredList.size();
        }

        @Override
        public schoolsDB.School getItem(int position) {
            return filteredList.get(position);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    
                    // אם אין מחרוזת חיפוש, מחזירים את כל הרשימה
                    if (constraint == null || constraint.length() == 0) {
                        results.values = originalList;
                        results.count = originalList.size();
                    } else {
                        List<schoolsDB.School> filteredSchools = new ArrayList<>();
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        
                        // Check if filter is a school ID
                        if (filterPattern.matches("\\d+") && filterPattern.length() <= 6) {
                            for (schoolsDB.School school : originalList) {
                                if (String.valueOf(school.getSchoolId()).startsWith(filterPattern)) {
                                    filteredSchools.add(school);
                                }
                            }
                        }
                        
                        // סינון בתי ספר לפי שם
                        for (schoolsDB.School school : originalList) {
                            if (school.getSchoolName().toLowerCase().contains(filterPattern)) {
                                // Don't add duplicates if already added by ID match
                                if (!filteredSchools.contains(school)) {
                                    filteredSchools.add(school);
                                }
                            }
                        }
                        
                        results.values = filteredSchools;
                        results.count = filteredSchools.size();
                    }
                    
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredList = (List<schoolsDB.School>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        
        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            
            // Make sure the text displayed is the school name
            android.widget.TextView text = (android.widget.TextView) view;
            schoolsDB.School school = getItem(position);
            text.setText(school.getSchoolName());
            
            return view;
        }
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
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }

    public void moveToRakazRegister(View view) {
        Intent intent = new Intent(this, rakazRegister.class);
        
        // Pass the selected school ID if available
        if (selectedSchool != null) {
            intent.putExtra("schoolId", String.valueOf(selectedSchool.getSchoolId()));
            intent.putExtra("schoolName", selectedSchool.getSchoolName());
        }
        
        startActivity(intent);
    }

    // For testing: Grant admin privileges to a rakaz user
    private void grantAdminPrivileges(String schoolId, String username) {
        fireDB.collection("schools").document(schoolId)
                .collection("rakazim").document(username)
                .update("isAdmin", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RakazLogin", "Admin privileges granted to " + username);
                    goToNextScreen();
                })
                .addOnFailureListener(e -> {
                    Log.e("RakazLogin", "Failed to grant admin privileges: " + e.getMessage());
                    goToNextScreen(); // Continue anyway
                });
    }
}
