package com.project.megamatch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class creditsPage extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.credits_page);
        
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.creditsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup contact button
        MaterialButton contactButton = findViewById(R.id.contactButton);
        contactButton.setOnClickListener(v -> showContactChooser());
        
        // Setup close button
        MaterialButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> finish());
    }

    private void showContactChooser() {
        Intent whatsappIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")); // Generic SMS intent
        if (isAppInstalled("com.whatsapp")) {
            whatsappIntent.setPackage("com.whatsapp"); // Set WhatsApp as an option if available
        }

        Intent gmailIntent = new Intent(Intent.ACTION_SEND); // Generic send intent
        gmailIntent.setType("text/plain");
        gmailIntent.setPackage("com.google.android.gm"); // Set Gmail as an option

        Intent chooser = Intent.createChooser(whatsappIntent, "בחר אפליקציה"); // "Choose an app"
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{gmailIntent}); // Add Gmail to chooser

        startActivity(chooser);
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
