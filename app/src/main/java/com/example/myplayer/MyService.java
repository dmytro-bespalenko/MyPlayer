package com.example.myplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyService extends Service {


    private MediaPlayer mediaPlayer;
    private static final String TAG = "My_LOG";
    private int currentSeekBarPosition;
    private List<Playlist> playList = new ArrayList<>();
    private List<Playlist> finalPlayList = new ArrayList<>();
    private int playPosition = 0;
    private Executor executor;
    private int clickOnSong;

    public static final String NEXT = "NEXT";
    public static final String PLAY = "PLAY";
    public static final String PAUSE = "PAUSE";

    @Override
    public void onCreate() {
        super.onCreate();

        executor = Executors.newSingleThreadExecutor();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotification();
        else
            startForeground(1, new Notification());

    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        switch (action) {
            case "LIST":
                playList = intent.getParcelableArrayListExtra("song");
                finalPlayList.addAll(playList);
                break;
            case PLAY:
                if (finalPlayList.size() > 0 && !mediaPlayer.isPlaying()) {
                    releaseMediaPlayer();
                    Log.d(TAG, Thread.currentThread().getName());
                    mediaPlayer = MediaPlayer.create(this, playList.get(clickOnSong).getId());
                    executor.execute(timeUpdaterRunnable);
                }

                break;
            case "STOP":
                Log.d(TAG, "STOP " + Thread.currentThread().getName());

                onDestroy();
                break;
            case PAUSE:
                Log.d(TAG, "PAUSE" + Thread.currentThread().getName());
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                        currentSeekBarPosition = mediaPlayer.getCurrentPosition();
                    }

                } else {

                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        mediaPlayer.seekTo(currentSeekBarPosition);
                        executor.execute(timeUpdaterRunnable);
                    }

                }

                break;
            case NEXT:
                Log.d(TAG, "NEXT" + Thread.currentThread().getName());
                if (finalPlayList.size() > 0) {
                    nextSong();
                }
                break;
            case "PROGRESS":
                Log.d(TAG, "progress" + intent.getExtras().getInt("progress"));
                mediaPlayer.seekTo(intent.getExtras().getInt("progress"));
                break;
            case "CUSTOM_SONG":
                clickOnSong = intent.getIntExtra("currentPosition", 0);

                playPosition = clickOnSong;
                if (mediaPlayer.isPlaying()) {
                    releaseMediaPlayer();
                }
                mediaPlayer = MediaPlayer.create(this, finalPlayList.get(clickOnSong).getId());

                Playlist firstSong = finalPlayList.get(0);

                if (playPosition != 0) {
                    Collections.swap(finalPlayList, 0, playPosition);
                    Collections.swap(finalPlayList, finalPlayList.indexOf(firstSong), finalPlayList.size() - 1);
                }

                playPosition = 0;
                sendFinalList();
                executor.execute(timeUpdaterRunnable);

                Log.d(TAG, "onStartCommand currentPosition: " + clickOnSong);
                break;
            case "LONG_CLICK":

                int deleteSong = intent.getIntExtra("longClickPosition", 0);
                finalPlayList.remove(deleteSong);
                sendFinalList();
                break;

        }
        return START_STICKY;
    }

    private void releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void nextSong() {

        playPosition++;

        if (playPosition == finalPlayList.size()) {
            playPosition = 0;
        }

        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(this, finalPlayList.get(playPosition).getId());

        Playlist playlistFirstSong = finalPlayList.get(0);
        Collections.swap(finalPlayList, playPosition, 0);
        Collections.swap(finalPlayList, finalPlayList.indexOf(playlistFirstSong), finalPlayList.size() - 1);
        sendFinalList();
        playPosition--;


        executor.execute(timeUpdaterRunnable);
        Log.d("execute ", "nextSong: " + Thread.currentThread().getName());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification() {

        String NOTIFICATION_CHANNEL_ID = "com.example.myplayer";

        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notifications);

        contentView.setImageViewResource(R.id.imagePlayer, R.mipmap.ic_launcher);
        contentView.setTextViewText(R.id.titleMyPlayer, "Music is playing");

        contentView.setImageViewResource(R.id.notiPlay, R.drawable.ic_baseline_play_arrow_24);
        contentView.setImageViewResource(R.id.notiPause, R.drawable.ic_baseline_pause_24);
        contentView.setImageViewResource(R.id.notiNext, R.drawable.ic_baseline_skip_next_24);

        Intent intent = new Intent(this, MyService.class);

        intent.setAction(NEXT);
        PendingIntent pendingNext = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notiNext, pendingNext);

        intent.setAction(PLAY);
        PendingIntent pendingPlay = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notiPlay, pendingPlay);

        intent.setAction(PAUSE);
        PendingIntent pendingPause = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notiPause, pendingPause);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setContent(contentView);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();

        startForeground(2, notification);

    }


    private final Runnable timeUpdaterRunnable = new Runnable() {
        @Override
        public void run() {
            mediaPlayer.start();
            while (mediaPlayer.isPlaying()) {
                try {
                    currentSeekBarPosition = mediaPlayer.getCurrentPosition();
                    Log.d(TAG, "run: " + this.toString() + " " + currentSeekBarPosition);
                    sendSeekBarMessage();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            if (mediaPlayer.getCurrentPosition() / 1000 >= mediaPlayer.getDuration() / 1000) {
                nextSong();
                Log.d(TAG, "run: " + "NEXT() " + " " + currentSeekBarPosition);
            }

        }
    };


    private void sendFinalList() {
        Intent intent = new Intent("song_list");
        intent.putParcelableArrayListExtra("finallist", (ArrayList<? extends Parcelable>) finalPlayList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("sendFinalList() ", "Broadcasting message");
    }


    private void sendSeekBarMessage() {
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("messageCurrentPosition", String.valueOf(currentSeekBarPosition));
        Log.d(TAG, "sendMessage: LocalBroadcastManager " + currentSeekBarPosition);

        intent.putExtra("messageMax", String.valueOf(mediaPlayer.getDuration()));
        Log.d(TAG, "sendMessage: LocalBroadcastManager " + mediaPlayer.getDuration());

        intent.putExtra("current_song", playPosition);
        Log.d(TAG, "sendMessage: LocalBroadcastManager " + playPosition);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        currentSeekBarPosition = 0;
        sendSeekBarMessage();
        mediaPlayer.stop();

        stopForeground(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}