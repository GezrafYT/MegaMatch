package com.example.megamatch;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class creditsPage extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credits_page);

        Button contactButton = findViewById(R.id.contactButton);

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactOptions();
            }
        });
    }

    private void showContactOptions() {
        boolean isWhatsAppInstalled = isAppInstalled("com.whatsapp");

        // Build the list of options dynamically
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר אפשרות"); // "Choose an option"

        // Options array (adjusted dynamically)
        String[] options;
        if (isWhatsAppInstalled) {
            options = new String[]{"Open WhatsApp", "Open SMS (הודעות)"};
        } else {
            options = new String[]{"Open SMS (הודעות)"};
        }

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // For now, do nothing when an option is clicked
            }
        });

        builder.show();
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
