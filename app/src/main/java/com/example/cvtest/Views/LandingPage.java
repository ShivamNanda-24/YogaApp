package com.example.cvtest.Views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cvtest.MainActivity;
import com.example.cvtest.R;

public class LandingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        ToggleButton yogaButton = findViewById(R.id.yogaButton);
        ToggleButton meditationButton = findViewById(R.id.meditationButton);
        final ScrollView yogaScrollView = findViewById(R.id.scroll);
        final FrameLayout meditationPlaceholder = findViewById(R.id.meditationPlaceholder);
        yogaButton.setChecked(true);
        yogaScrollView.setVisibility(View.VISIBLE);
        meditationPlaceholder.setVisibility(View.GONE);

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);


        button1.setOnClickListener(view -> openMainActivity("Tree Pose"));
        button2.setOnClickListener(view -> openMainActivity("Warrior Pose"));
        button3.setOnClickListener(view -> openMainActivity("Routine"));


        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView == yogaButton) {
                    if (isChecked) {
                        yogaScrollView.setVisibility(View.VISIBLE);
                        meditationPlaceholder.setVisibility(View.GONE);
                        meditationButton.setChecked(false);
                    }
                } else if (buttonView == meditationButton) {
                    if (isChecked) {
                        meditationPlaceholder.setVisibility(View.VISIBLE);
                        yogaScrollView.setVisibility(View.GONE);
                        yogaButton.setChecked(false);
                    }
                }
            }
        };

        yogaButton.setOnCheckedChangeListener(listener);
        meditationButton.setOnCheckedChangeListener(listener);

        // Initial visibility setup
        yogaScrollView.setVisibility(yogaButton.isChecked() ? View.VISIBLE : View.GONE);
        meditationPlaceholder.setVisibility(meditationButton.isChecked() ? View.VISIBLE : View.GONE);
    }

    public void openMainActivity(String buttonPressed) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("button_pressed", buttonPressed);
        startActivity(intent);
    }
}
