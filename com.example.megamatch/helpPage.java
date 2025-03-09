package com.example.megamatch;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.megamatch.PageFragment;

public class helpPage extends AppCompatActivity {

    private Button page1Button, page2Button, page3Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_page);

        // Initialize buttons
        page1Button = findViewById(R.id.page1Button);
        page2Button = findViewById(R.id.page2Button);
        page3Button = findViewById(R.id.page3Button);

        // Set default fragment (Page 1)
        replaceFragment(PageFragment.newInstance("Page 1"));

        // Set button click listeners
        page1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(PageFragment.newInstance("Page 1"));
            }
        });

        page2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(PageFragment.newInstance("Page 2"));
            }
        });

        page3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(PageFragment.newInstance("Page 3"));
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.helpFragmentContainer, fragment);
        transaction.commit();
    }
}
