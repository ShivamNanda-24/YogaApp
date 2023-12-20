package com.example.cvtest.Views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.cvtest.MainActivity;
import com.example.cvtest.R;

public class CompletionPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completion_page);

        Button btnGoBack = findViewById(R.id.btnGoBack);
        Button btnRedo = findViewById(R.id.btnRedo);

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CompletionPage.this, LandingPage.class);
                // Start the activity
                startActivity(intent);
            }
        });

        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Re do button click
            }
        });
    }
}