package com.project.megamatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class rakazPage extends AppCompatActivity {

    private TextView welcomeText;
    private Button manageMegamaButton;
    private Button viewMegamaButton;
    private Button logoutButton;
    private Button manageRakazEmailsButton;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore fireDB;
    private String schoolId;
    private String username;
    private boolean hasMegama = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.rakaz_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rakazPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // קישור לרכיבי UI
        welcomeText = findViewById(R.id.welcomeText);
        manageMegamaButton = findViewById(R.id.manageMegamaButton);
        viewMegamaButton = findViewById(R.id.viewMegamaButton);
        logoutButton = findViewById(R.id.logoutButton);
        manageRakazEmailsButton = findViewById(R.id.manageRakazEmailsButton);
        
        // אתחול פיירבייס
        fireDB = FirebaseFirestore.getInstance();
        
        // קבלת נתוני שיתוף
        sharedPreferences = getSharedPreferences("MegaMatchPrefs", Context.MODE_PRIVATE);
        
        // קבלת נתוני משתמש מחובר
        schoolId = sharedPreferences.getString("loggedInSchoolId", "");
        username = sharedPreferences.getString("loggedInUsername", "");
        
        // בדיקה שהנתונים תקינים
        if (schoolId.isEmpty() || username.isEmpty()) {
            Intent intent = new Intent(this, loginPage.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // קבלת שם מלא של הרכז וגם מידע אם יש לו מגמה
        loadRakazDetails();
        
        // הגדרת כפתורים
        initializeViews();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check if megama status has changed when returning to this activity
        loadRakazDetails();
    }
    
    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        manageMegamaButton = findViewById(R.id.manageMegamaButton);
        viewMegamaButton = findViewById(R.id.viewMegamaButton);
        logoutButton = findViewById(R.id.logoutButton);
        manageRakazEmailsButton = findViewById(R.id.manageRakazEmailsButton);
        
        // Set button click listeners
        manageMegamaButton.setOnClickListener(v -> manageMegama());
        viewMegamaButton.setOnClickListener(v -> viewMegama());
        logoutButton.setOnClickListener(v -> logoutRakaz());
        manageRakazEmailsButton.setOnClickListener(v -> openRakazEmailsManagement());
        
        // Initially disable view megama button until we check if the rakaz has a megama
        viewMegamaButton.setEnabled(false);
        
        // Check for admin privileges
        checkAdminStatus();
    }
    
    // פונקציה לטעינת פרטי הרכז
    private void loadRakazDetails() {
        fireDB.collection("schools").document(schoolId)
              .collection("rakazim").document(username)
              .get()
              .addOnSuccessListener(documentSnapshot -> {
                  if (documentSnapshot.exists()) {
                      String firstName = documentSnapshot.getString("firstName");
                      String lastName = documentSnapshot.getString("lastName");
                      String megamaName = documentSnapshot.getString("megama");
                      
                      if (firstName != null && lastName != null && !firstName.isEmpty() && !lastName.isEmpty()) {
                          welcomeText.setText("שלום, " + firstName + " " + lastName + "!");
                      } else {
                          welcomeText.setText("שלום, " + username + "!");
                      }
                      
                      // Check if rakaz has a megama
                      hasMegama = (megamaName != null && !megamaName.isEmpty());
                      viewMegamaButton.setEnabled(hasMegama);
                  } else {
                      welcomeText.setText("שלום, " + username + "!");
                      viewMegamaButton.setEnabled(false);
                  }
              })
              .addOnFailureListener(e -> {
                  Log.e("RakazPage", "Error loading rakaz details: " + e.getMessage());
                  welcomeText.setText("שלום, " + username + "!");
                  viewMegamaButton.setEnabled(false);
              });
              
        // Also check if megama exists in the megamot collection
        fireDB.collection("schools").document(schoolId)
              .collection("megamot")
              .whereEqualTo("rakazUsername", username)
              .get()
              .addOnSuccessListener(querySnapshot -> {
                  hasMegama = !querySnapshot.isEmpty();
                  viewMegamaButton.setEnabled(hasMegama);
              })
              .addOnFailureListener(e -> {
                  Log.e("RakazPage", "Error checking megama: " + e.getMessage());
              });
    }
    
    // פונקציה לניהול המגמה (יצירה או עדכון)
    private void manageMegama() {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Check if user already has a megama
        db.collection("schools").document(schoolId)
          .collection("users").document(username)
          .get()
          .addOnSuccessListener(userDoc -> {
              if (userDoc.exists() && userDoc.contains("megamaName")) {
                  String megamaName = userDoc.getString("megamaName");
                  if (megamaName != null && !megamaName.isEmpty()) {
                      // User already has a megama - get it and send to update
                      db.collection("schools").document(schoolId)
                        .collection("megamot").document(megamaName)
                        .get()
                        .addOnSuccessListener(megamaDoc -> {
                            if (megamaDoc.exists()) {
                                // Initialize intent for update
                                Intent intent = new Intent(rakazPage.this, megamaCreate.class);
                                intent.putExtra("isUpdate", true);
                                intent.putExtra("schoolId", schoolId);
                                intent.putExtra("username", username);
                                
                                // Pass existing megama data
                                String existingName = megamaDoc.getString("name");
                                String existingDescription = megamaDoc.getString("description");
                                
                                intent.putExtra("megamaName", existingName);
                                intent.putExtra("megamaDescription", existingDescription);
                                
                                // Pass condition data
                                Boolean requiresExam = megamaDoc.getBoolean("requiresExam");
                                intent.putExtra("requiresExam", requiresExam != null ? requiresExam : false);
                                
                                Boolean requiresGradeAvg = megamaDoc.getBoolean("requiresGradeAvg");
                                intent.putExtra("requiresGradeAvg", requiresGradeAvg != null ? requiresGradeAvg : false);
                                
                                Long requiredGradeAvgLong = megamaDoc.getLong("requiredGradeAvg");
                                int requiredGradeAvg = requiredGradeAvgLong != null ? requiredGradeAvgLong.intValue() : 0;
                                intent.putExtra("requiredGradeAvg", requiredGradeAvg);
                                
                                // Pass custom conditions
                                ArrayList<String> customConditions = (ArrayList<String>) megamaDoc.get("customConditions");
                                if (customConditions != null) {
                                    intent.putStringArrayListExtra("customConditions", customConditions);
                                }
                                
                                // Pass image URLs for the next screen
                                ArrayList<String> imageUrls = (ArrayList<String>) megamaDoc.get("imageUrls");
                                if (imageUrls != null) {
                                    intent.putStringArrayListExtra("imageUrls", imageUrls);
                                }
                                
                                startActivity(intent);
                            } else {
                                // Megama reference exists but megama doesn't - create new
                                startMegamaCreation();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error retrieving megama - create new
                            Toast.makeText(rakazPage.this, "שגיאה בטעינת מגמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            startMegamaCreation();
                        });
                  } else {
                      // No megama name - create new
                      startMegamaCreation();
                  }
              } else {
                  // No megama reference - create new
                  startMegamaCreation();
              }
          })
          .addOnFailureListener(e -> {
              // Error - create new
              Toast.makeText(rakazPage.this, "שגיאה בבדיקת מגמה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
              startMegamaCreation();
          });
    }
    
    private void startMegamaCreation() {
        // Start megama creation flow
        Intent intent = new Intent(rakazPage.this, megamaCreate.class);
        intent.putExtra("isUpdate", false);
        intent.putExtra("schoolId", schoolId);
        intent.putExtra("username", username);
        startActivity(intent);
    }
    
    // פונקציה לצפייה במגמה
    private void viewMegama() {
        if (hasMegama) {
            Intent intent = new Intent(this, MegamaPreview.class);
            intent.putExtra("schoolId", schoolId);
            intent.putExtra("username", username);
            startActivity(intent);
        } else {
            Toast.makeText(this, "אין לך מגמה לצפייה כרגע", Toast.LENGTH_SHORT).show();
        }
    }

    // התנתקות מהמערכת
    private void logoutRakaz() {
        clearSessionData();
        Intent intent = new Intent(this, loginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ניקוי נתוני חיבור
    private void clearSessionData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Check if the rakaz user has admin privileges
    private void checkAdminStatus() {
        fireDB.collection("schools").document(schoolId)
              .collection("rakazim").document(username)
              .get()
              .addOnSuccessListener(documentSnapshot -> {
                  if (documentSnapshot.exists()) {
                      Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                      if (isAdmin != null && isAdmin) {
                          // Show admin button for managing rakaz emails
                          manageRakazEmailsButton.setVisibility(View.VISIBLE);
                      } else {
                          manageRakazEmailsButton.setVisibility(View.GONE);
                      }
                  } else {
                      manageRakazEmailsButton.setVisibility(View.GONE);
                  }
              })
              .addOnFailureListener(e -> {
                  Log.e("RakazPage", "Error checking admin status: " + e.getMessage());
                  manageRakazEmailsButton.setVisibility(View.GONE);
              });
    }

    // Open the admin page for managing rakaz emails
    private void openRakazEmailsManagement() {
        Intent intent = new Intent(this, AdminRakazEmailsActivity.class);
        intent.putExtra("schoolId", schoolId);
        startActivity(intent);
    }
}
