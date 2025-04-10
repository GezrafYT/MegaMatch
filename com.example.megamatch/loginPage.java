package com.example.megamatch;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;

import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;
import com.bumptech.glide.Glide;
import android.Manifest;
import androidx.core.app.ActivityCompat;


public class loginPage extends AppCompatActivity {
    private ImageView image1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
//        image1 = findViewById(R.id.image1);

        ImageView giraffeGif = findViewById(R.id.giraffeGif);
        Glide.with(this)
                .asGif() // Ensure it’s loaded as a GIF
                .load(R.drawable.giraffe_gif) // Load the GIF from the drawable
                .into(giraffeGif);


        if (!checkPermission(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }


    public void popMessage(View view) {
        Toast.makeText(this, "זוהי הודעה", Toast.LENGTH_LONG).show();
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



//    public void wait(int seconds)
//    {
//        try {
//            Thread.sleep(seconds);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

//    public void changeTextColor(View view)
//    {
//        Button button = (Button) view;
//        button.setTextColor(Color.rgb(255, 0, 0));
//    }

//    public void moveToScreen3Delayed(View view)
//    {
//        wait(3000);
//        Intent i1 = new Intent(this, MainActivity3.class);
//        startActivity(i1);
//    }

//    public void zoomImage(View view) {
//        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
//        image1.startAnimation(zoomIn);
//    }


}
