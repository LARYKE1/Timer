package com.example.timer;

import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private NumberPicker mHoursPicker;
    private NumberPicker mMinutesPicker;
    private NumberPicker mSecondsPicker;
    private Button mStartButton;
    private Button mPauseButton;
    private Button mCancelButton;
    private ProgressBar mProgressBar;
    private TextView mTimeLeftTextView;
    private Handler mHandler;
    private TimerModel mTimerModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTimeLeftTextView = findViewById(R.id.time_left_text_view);
        mTimeLeftTextView.setVisibility(View.INVISIBLE);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mStartButton = findViewById(R.id.start_button);
        mPauseButton = findViewById(R.id.pause_button);
        mCancelButton = findViewById(R.id.cancel_button);
        mStartButton.setOnClickListener(this::startButtonClick);
        mPauseButton.setOnClickListener(this::pauseButtonClick);
        mCancelButton.setOnClickListener(this::cancelButtonClick);
        mPauseButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        NumberPicker.Formatter numFormat = i -> new
                DecimalFormat("00").format(i);
        mHoursPicker = findViewById(R.id.hours_picker);
        mHoursPicker.setMinValue(0);
        mHoursPicker.setMaxValue(99);

        mHoursPicker.setFormatter(numFormat);
        mMinutesPicker = findViewById(R.id.minutes_picker);
        mMinutesPicker.setMinValue(0);
        mMinutesPicker.setMaxValue(59);
        mMinutesPicker.setFormatter(numFormat);
        mSecondsPicker = findViewById(R.id.seconds_picker);
        mSecondsPicker.setMinValue(0);
        mSecondsPicker.setMaxValue(59);
        mSecondsPicker.setFormatter(numFormat);
        mTimerModel = new TimerModel();
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void startButtonClick(View view) {
        int hours = mHoursPicker.getValue();
        int minutes = mMinutesPicker.getValue();
        int seconds = mSecondsPicker.getValue();
        if (hours + minutes + seconds > 0) {
            mTimeLeftTextView.setVisibility(VISIBLE);
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(VISIBLE);
            mStartButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(VISIBLE);
            mPauseButton.setText(R.string.pause);
            mCancelButton.setVisibility(VISIBLE);
            mTimerModel.start(hours, minutes, seconds);
            mHandler.post(mUpdateTimerRunnable);
        }
    }

    private void pauseButtonClick(View view) {

        if (mTimerModel.isRunning()) {
            mTimerModel.pause();
            mHandler.removeCallbacks(mUpdateTimerRunnable);
            mPauseButton.setText(R.string.resume);
        }
        else {
            mTimerModel.resume();
            mHandler.post(mUpdateTimerRunnable);
            mPauseButton.setText(R.string.pause);
        }


    }

    private void cancelButtonClick(View view) {
        mTimeLeftTextView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        timerCompleted();
    }

    private void timerCompleted() {
        mTimerModel.stop();
        mHandler.removeCallbacks(mUpdateTimerRunnable);
        mStartButton.setVisibility(VISIBLE);
        mPauseButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
    }

    private final Runnable mUpdateTimerRunnable = new Runnable() {
        @Override
        public void run() {

            mTimeLeftTextView.setText(mTimerModel.toString());
            int progress = mTimerModel.getProgressPercent();
            mProgressBar.setProgress(progress);

            if (progress == 100) {
                timerCompleted();
            } else {
                mHandler.postDelayed(this, 200);
            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();

        if (mTimerModel.isRunning()) {
            WorkRequest timerWorkRequest = new
                    OneTimeWorkRequest.Builder(TimerWorker.class)
                    .setInputData(new Data.Builder()
                            .putLong(TimerWorker.KEY_MILLISECONDS_REMAINING,
                                    mTimerModel.getRemainingMilliseconds())
                            .build()
                    ).build();
            WorkManager.getInstance(this).enqueue(timerWorkRequest);
        }
    }
}