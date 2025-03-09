package com.example.megamatch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
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
                showContactChooser();
            }
        });
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
