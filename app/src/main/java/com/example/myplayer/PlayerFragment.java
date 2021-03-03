package com.example.myplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PlayerFragment extends Fragment  {

    private static final String TAG = "My_LOG";
    private SeekBar seekBar;
    private int progressValue;
    private TextView mTextView;
    private TextView durationTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_player, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = view.findViewById(R.id.progressBar);
        durationTextView= view.findViewById(R.id.durationBar);
        final FloatingActionButton btnPlay = view.findViewById(R.id.buttonPlay);
        final FloatingActionButton btnStop = view.findViewById(R.id.buttonStop);
        final FloatingActionButton btnPause = view.findViewById(R.id.buttonPause);
        final FloatingActionButton btnNext = view.findViewById(R.id.buttonNext);
        seekBar = view.findViewById(R.id.seekBar);

        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MyService.class);
                intent.setAction("PLAY");
                Log.d(TAG, "onClick: PLAY");
                Objects.requireNonNull(getActivity()).startService(intent);

            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), MyService.class);
                intent.setAction("STOP");
                Log.d(TAG, "onClick: STOP");
                Objects.requireNonNull(getActivity()).startService(intent);

            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MyService.class);
                intent.setAction("PAUSE");
                Log.d(TAG, "onClick: PAUSE");
                Objects.requireNonNull(getActivity()).startService(intent);


            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MyService.class);
                intent.setAction("NEXT");
                Log.d(TAG, "onClick: NEXT");
                Objects.requireNonNull(getActivity()).startService(intent);

            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                mTextView.post(mUpdateTime);

                if (!fromUser) {
                    return;
                }
                Intent intent = new Intent(getActivity(), MyService.class);
                intent.setAction("PROGRESS");

                intent.putExtra("progress", progressValue);
                Objects.requireNonNull(getActivity()).startService(intent);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String messageMaxDuration = intent.getStringExtra("messageMax");
            seekBar.setMax(Integer.parseInt(messageMaxDuration));
            int messageCurrentPosition = Integer.parseInt(intent.getStringExtra("messageCurrentPosition"));
            Log.d("MAX", "messageMaxDuration = [" + messageMaxDuration + "], messageCurrentPosition = [" + messageCurrentPosition + "]");
            seekBar.setProgress(messageCurrentPosition);

            durationTextView.setText(milliSecondsToTimer(Long.parseLong(messageMaxDuration)));

        }
    };


    Runnable mUpdateTime = new Runnable() {
        public void run() {
            updatePlayer(progressValue);
            mTextView.postDelayed(this, 1000);

        }
    };


    private void updatePlayer(int currentDuration) {
        mTextView.setText(milliSecondsToTimer(currentDuration));

    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).unregisterReceiver(mMessageReceiver);
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

}