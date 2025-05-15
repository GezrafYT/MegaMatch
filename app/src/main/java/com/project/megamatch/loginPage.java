package com.project.megamatch;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;

import android.widget.ImageView;
import android.content.Intent;
import com.bumptech.glide.Glide;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class loginPage extends AppCompatActivity {
    private ImageView image1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!checkPermission(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }


    public void moveToTalmidLogin(View view)
    {
        Intent i1 = new Intent(this, talmidLogin.class);
        startActivity(i1);
    }

    public void moveToRakazLogin(View view)
    {
        Intent i1 = new Intent(this, rakazLogin.class);
        startActivity(i1);
    }


    public void moveToHelp(View view)
    {
        Intent i1 = new Intent(this, helpPage.class);
        startActivity(i1);
    }

    public void moveToCredits(View view)
    {
        Intent i1 = new Intent(this, creditsPage.class);
        startActivity(i1);
    }


    public boolean checkPermission(String permission)
    {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

}