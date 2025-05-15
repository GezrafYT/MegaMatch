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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class talmidLogin extends AppCompatActivity {

    private AutoCompleteTextView schoolAutocomplete;
    private Button talmidLoginButton;
    private FirebaseFirestore fireDB;
    private SharedPreferences sharedPreferences;
    private List<schoolsDB.School> allSchools;
    private SchoolAdapter schoolAdapter;
    private schoolsDB.School selectedSchool;

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
        schoolAutocomplete = findViewById(R.id.schoolAutocomplete);
        talmidLoginButton = findViewById(R.id.talmidLoginButton);

        fireDB = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("MegaMatchPrefs", MODE_PRIVATE);

        // הכנת רשימת בתי הספר להצגה
        setupSchoolAutocomplete();

        // לחיצה על כפתור צפייה
        talmidLoginButton.setOnClickListener(new View.OnClickListener() {
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
                
                if (selectedSchool != null) {
                    // Check if school exists in Firestore before proceeding
                    verifySchoolInFirestore(String.valueOf(selectedSchool.getSchoolId()));
                } else {
                    Toast.makeText(talmidLogin.this, "נא לבחור בית ספר", Toast.LENGTH_SHORT).show();
                    
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

        public SchoolAdapter(talmidLogin context, int resource, List<schoolsDB.School> objects) {
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

    // שמירת פרטי בית הספר
    private void saveSchoolInfo(String schoolId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("loggedInSchoolId", schoolId);
        editor.putString("viewingAsGuest", "true");
        editor.apply();
    }

    // מעבר למסך הבא
    private void goToNextScreen() {
        Intent intent = new Intent(this, talmidPage.class);
        startActivity(intent);
        finish();
    }

    // בדיקה אם בית הספר קיים במסד הנתונים
    private void verifySchoolInFirestore(String schoolId) {
        // Show loading indicator
        View loadingOverlay = findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
        
        // Add logging to debug
        Log.d("SchoolVerification", "Checking school ID in Firestore: " + schoolId);
        
        // For testing purposes, allow all schools temporarily
        // This is for testing only - remove in production
        saveSchoolInfo(schoolId);
        goToNextScreen();
        
        /* Temporarily commented out for debugging
        fireDB.collection("schools").document(schoolId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                // Hide loading indicator
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(View.GONE);
                }
                
                Log.d("SchoolVerification", "Document exists: " + documentSnapshot.exists());
                if (documentSnapshot.exists()) {
                    // שמירת סימול בית הספר והפניה לדף הבא
                    saveSchoolInfo(schoolId);
                    goToNextScreen();
                } else {
                    Toast.makeText(talmidLogin.this, "בית הספר אינו רשום במערכת", Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> {
                // Hide loading indicator
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(View.GONE);
                }
                
                Log.e("FirebaseError", "Error checking school: " + e.getMessage());
                Toast.makeText(talmidLogin.this, "שגיאה בבדיקת בית הספר, נסה שנית", Toast.LENGTH_LONG).show();
            });
        */
    }
}
