package com.example.cvtest.Views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cvtest.MainActivity;
import com.example.cvtest.R;

public class LandingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);

        button1.setOnClickListener(view -> openMainActivity("Button1"));
        button2.setOnClickListener(view -> openMainActivity("Button2"));
    }

    private void openMainActivity(String buttonPressed) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("button_pressed", buttonPressed);
        startActivity(intent);
    }
}
