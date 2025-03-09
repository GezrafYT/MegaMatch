package com.example.megamatch;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class helpPage extends AppCompatActivity {

    private Button page1Button, page2Button, page3Button, page4Button, page5Button, page6Button;
    private TextFragment textFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_page);

        page1Button = findViewById(R.id.page1Button);
        page2Button = findViewById(R.id.page2Button);
        page3Button = findViewById(R.id.page3Button);
        page4Button = findViewById(R.id.page4Button);
        page5Button = findViewById(R.id.page5Button);
        page6Button = findViewById(R.id.page6Button);

        // Initialize and add the TextFragment
        textFragment = new TextFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.helpFragmentContainer, textFragment);
        fragmentTransaction.commit();

        // Click listeners to update text content
        page1Button.setOnClickListener(v -> textFragment.updateText("Page 1"));
        page2Button.setOnClickListener(v -> textFragment.updateText("Page 2"));
        page3Button.setOnClickListener(v -> textFragment.updateText("Page 3"));
        page4Button.setOnClickListener(v -> textFragment.updateText("Page 4"));
        page5Button.setOnClickListener(v -> textFragment.updateText("Page 5"));
        page6Button.setOnClickListener(v -> textFragment.updateText("Page 6"));
    }
}
