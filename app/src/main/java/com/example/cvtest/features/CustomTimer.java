package com.example.cvtest.features;

import android.os.Handler;

public class CustomTimer {
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isRunning = false;
    private long interval;
    private TimerListener listener;

    public interface TimerListener {
        void onTick(long millisUntilFinished);
        void onFinish(); // New method to notify when the timer finishes
    }

    public CustomTimer(long millisInFuture, long countDownInterval, TimerListener listener) {
        this.interval = countDownInterval;
        this.listener = listener;

        runnable = new Runnable() {
            long millisUntilFinished = millisInFuture;

            @Override
            public void run() {
                if (millisUntilFinished <= 0) {
                    isRunning = false;
                    if (listener != null) {
                        listener.onFinish(); // Notify that the timer has finished
                    }
                    return;
                }

                if (listener != null) {
                    listener.onTick(millisUntilFinished);
                }

                millisUntilFinished -= interval;
                handler.postDelayed(this, interval);
            }
        };
    }

    public void start() {
        if (!isRunning) {
            handler.postDelayed(runnable, interval);
            isRunning = true;
        }
    }

    public void pause() {
        if (isRunning) {
            handler.removeCallbacks(runnable);
            isRunning = false;
        }
    }

    public void reset(long millisInFuture) {
        pause();
        runnable = new Runnable() {

            @Override
            public void run() {
                // same implementation as above
            }
        };
    }

    public boolean isRunning() {
        return isRunning;
    }
}
