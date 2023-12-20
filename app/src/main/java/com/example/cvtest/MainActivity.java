package com.example.cvtest;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.cvtest.Views.OverlayView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech tts;
    private TextView textView;
    private TextView textViewTimer;
    private TextView textViewTimerText;
    private String prevMessage;
    private ImageView myImageView;
    private TextView timer;
    private MainViewModel viewModel;
    private boolean isTTSspeaking = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String buttonPressed = getIntent().getStringExtra("button_pressed");
        OverlayView.setButtonPressedValue(buttonPressed);

        myImageView = findViewById(R.id.myImageView);
        textView = findViewById(R.id.textView);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewTimerText = findViewById(R.id.textViewTimerText);

        if (buttonPressed.equals("Tree Pose")) {
            myImageView.setImageResource(R.drawable.image);
            // Set image dimensions
            ViewGroup.LayoutParams params = myImageView.getLayoutParams();
            params.height = convertDpToPx(250);
            params.width = convertDpToPx(150);
            myImageView.setLayoutParams(params);
        } else {
            myImageView.setImageResource(R.drawable.warrior_pose__1_);
            // Set image dimensions
            ViewGroup.LayoutParams params = myImageView.getLayoutParams();
            params.height = convertDpToPx(200);
            params.width = convertDpToPx(180);
            myImageView.setLayoutParams(params);


        }


        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.UK);
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



            if (tts != null && message != null && !message.equals(prevMessage) && !isTTSspeaking) {
//                int queueMode = tts.isSpeaking() ? TextToSpeech.QUEUE_ADD : TextToSpeech.QUEUE_FLUSH;
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
            }
            prevMessage = message;
        });

        OverlayView.getTimer().observe(this, newTime -> {
            textViewTimer.setText(newTime);
        });

        OverlayView.getTimerStatus().observe( this, timerStatus ->{
            if (timerStatus){
                textViewTimerText.setText("Paused");
            }
            else {
                textViewTimerText.setText("");
            }
        });
    }


    private int convertDpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
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
