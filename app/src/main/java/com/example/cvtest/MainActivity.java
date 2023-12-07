package com.example.cvtest;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech tts;
    private TextView textView;
    private TextView textViewTimer;
    private TextView textViewTimerText;
    private String prevMessage;
    private TextView timer;
    private MainViewModel viewModel;
    private boolean isTTSspeaking = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String buttonPressed = getIntent().getStringExtra("button_pressed");

        textView = findViewById(R.id.textView);
        textViewTimer = findViewById(R.id.textViewTimer);
//        textViewTimerText = findViewById(R.id.textViewTimerText);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.UK);
                tts.setPitch(1F);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        isTTSspeaking = true;
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        isTTSspeaking = false;
                    }
                    @Override
                    public void onError(String utteranceId) {
                        isTTSspeaking = false;
                    }
                });
            }
        });


        OverlayView.getMessage().observe(this, message -> {
            textView.setText(message);

            if ("Paused".equals(message)){
                textViewTimerText.setText(message);
            }

            if (tts != null && message != null && !message.equals(prevMessage) && !isTTSspeaking) {
//                int queueMode = tts.isSpeaking() ? TextToSpeech.QUEUE_ADD : TextToSpeech.QUEUE_FLUSH;
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
            }
            prevMessage = message;
        });

        OverlayView.getTimer().observe(this, newTime -> {
            textViewTimer.setText(newTime); // Update the TextView with the new time
        });
    }


    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
