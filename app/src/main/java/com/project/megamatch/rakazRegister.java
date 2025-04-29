package com.project.megamatch;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class rakazRegister extends AppCompatActivity {

    private AutoCompleteTextView schoolAutocomplete;
    private EditText firstNameInput, lastNameInput, emailInput, usernameInput, passwordInput, confirmPasswordInput;
    private ProgressBar progressBar;
    private FirebaseFirestore fireDB;
    private static final String TAG = "RakazRegister";
    private List<schoolsDB.School> allSchools;
    private SchoolAdapter schoolAdapter;
    private schoolsDB.School selectedSchool;

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

        // קישור לרכיבי UI
        schoolAutocomplete = findViewById(R.id.schoolAutocomplete);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        progressBar = findViewById(R.id.progressBar);

        // אתחול FirebaseFirestore
        fireDB = FirebaseFirestore.getInstance();
        
        // Load schools data
        schoolsDB.loadSchoolsFromCSV(this);
        
        // Setup school autocomplete
        setupSchoolAutocomplete();
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

        public SchoolAdapter(rakazRegister context, int resource, List<schoolsDB.School> objects) {
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

    // פונקציה להרשמת רכז
    public void registerRakaz(View view) {
        // בדיקת תקינות הקלט
        if (!validateInput()) {
            return;
        }

        // הצגת סרגל התקדמות
        progressBar.setVisibility(View.VISIBLE);

        // קבלת ערכי הקלט
        String schoolId = String.valueOf(selectedSchool.getSchoolId());
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // בדיקה האם האימייל ברשימת האימיילים המאושרים
        checkApprovedEmail(schoolId, email, firstName, lastName, username, password);
    }

    // בדיקת תקינות הקלט
    private boolean validateInput() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // בדיקת שדות ריקים
        if (selectedSchool == null || TextUtils.isEmpty(firstName) ||
                TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(username) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {
            if (selectedSchool == null) {
                Toast.makeText(this, "נא לבחור בית ספר", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        // בדיקת תקינות האימייל
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "נא להזין כתובת אימייל תקינה", Toast.LENGTH_SHORT).show();
            return false;
        }

        // בדיקת אורך שם המשתמש והסיסמה
        if (username.length() < 4) {
            Toast.makeText(this, "שם המשתמש חייב להיות באורך של 4 תווים לפחות", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "הסיסמה חייבת להיות באורך של 6 תווים לפחות", Toast.LENGTH_SHORT).show();
            return false;
        }

        // בדיקת התאמה בין סיסמאות
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // בדיקה האם האימייל מאושר ברשימת הרכזים המורשים
    private void checkApprovedEmail(String schoolId, String email, String firstName, String lastName, String username, String password) {
        fireDB.collection("schools").document(schoolId)
                .collection("allowedRakazEmails").document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean approved = documentSnapshot.getBoolean("approved");
                        Boolean registered = documentSnapshot.getBoolean("registered");

                        if (approved != null && approved && (registered == null || !registered)) {
                            // בדיקה האם שם משתמש כבר קיים
                            checkUsernameExists(schoolId, email, firstName, lastName, username, password);
                        } else if (registered != null && registered) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(rakazRegister.this, "אימייל זה כבר רשום במערכת", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(rakazRegister.this, "אימייל זה אינו מאושר להרשמה", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(rakazRegister.this, "אימייל זה לא נמצא ברשימת המורשים להרשמה", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(rakazRegister.this, "שגיאה בבדיקת אימייל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error checking email: " + e.getMessage());
                });
    }

    // בדיקה האם שם המשתמש כבר קיים
    private void checkUsernameExists(String schoolId, String email, String firstName, String lastName, String username, String password) {
        fireDB.collection("schools").document(schoolId)
                .collection("rakazim").document(username)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // שם המשתמש פנוי, ניתן להירשם
                        registerNewRakaz(schoolId, email, firstName, lastName, username, password);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(rakazRegister.this, "שם משתמש זה כבר קיים במערכת", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(rakazRegister.this, "שגיאה בבדיקת שם משתמש: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error checking username: " + e.getMessage());
                });
    }

    // רישום רכז חדש בפיירבייס
    private void registerNewRakaz(String schoolId, String email, String firstName, String lastName, String username, String password) {
        // יצירת מסמך משתמש חדש
        Map<String, Object> rakazData = new HashMap<>();
        rakazData.put("firstName", firstName);
        rakazData.put("lastName", lastName);
        rakazData.put("email", email);
        rakazData.put("password", password);

        // שמירת פרטי הרכז בפיירבייס
        fireDB.collection("schools").document(schoolId)
                .collection("rakazim").document(username)
                .set(rakazData)
                .addOnSuccessListener(aVoid -> {
                    // עדכון סטטוס 'registered' למייל
                    updateEmailRegistrationStatus(schoolId, email, username);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(rakazRegister.this, "שגיאה ברישום: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error registering user: " + e.getMessage());
                });
    }

    // עדכון סטטוס הרישום של האימייל
    private void updateEmailRegistrationStatus(String schoolId, String email, String username) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("registered", true);
        updates.put("username", username);

        fireDB.collection("schools").document(schoolId)
                .collection("allowedRakazEmails").document(email)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(rakazRegister.this, "הרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                    
                    // מעבר למסך הבא
                    goToLoginScreen();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(rakazRegister.this, "שגיאה בעדכון סטטוס הרשמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating registration status: " + e.getMessage());
                });
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(this, rakazLogin.class);
        startActivity(intent);
        finish();
    }
}
