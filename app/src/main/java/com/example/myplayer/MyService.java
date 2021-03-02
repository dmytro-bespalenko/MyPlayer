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
    private int currentSeekBarPoss;
    private List<Playlist> playList = new ArrayList<>();
    private List<Playlist> songList = new ArrayList<>();
    List<Playlist> finalList = new ArrayList<>();
    private int playPosition = 0;
    private Executor executor;
    private int currentSong;

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
                songList.addAll(playList);
                break;

            case PLAY:

                if (mediaPlayer.isPlaying()) {
                    break;
                }
                Log.d(TAG, Thread.currentThread().getName());
                executor.execute(timeUpdaterRunnable);
                break;
            case "STOP":
                Log.d(TAG, "STOP " + Thread.currentThread().getName());
                onDestroy();
                break;
            case PAUSE:
                Log.d(TAG, "PAUSE" + Thread.currentThread().getName());
                mediaPlayer.pause();
                break;
            case NEXT:
                Log.d(TAG, "NEXT" + Thread.currentThread().getName());
                mediaPlayer.stop();
                next();
                break;
            case "PROGRESS":
                Log.d(TAG, "progress" + intent.getExtras().getInt("progress"));
                int progress = intent.getExtras().getInt("progress");
                mediaPlayer.seekTo(progress);
                break;
            case "custom_song":

                currentSong = intent.getIntExtra("currentPosition", 0);
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                }

                playPosition = currentSong;

                finalList.add(0, songList.get(playPosition));

                for (int i = 0; i < songList.size(); i++) {
                    if (!songList.get(i).equals(songList.get(playPosition))) {
                        finalList.add(songList.get(i));
                        Log.d("songList.get(i)", "onStartCommand: songList.get(i)" + songList.get(i));
                    }
                }


                mediaPlayer = MediaPlayer.create(this, finalList.get(playPosition).getId());

                sendFinalList();
                executor.execute(timeUpdaterRunnable);

                Log.d(TAG, "onStartCommand currentPosition: " + currentSong);
                break;

        }
        return START_REDELIVER_INTENT;
    }

    public void next() {
        playPosition++;

        if (playPosition == finalList.size()) {
            playPosition = 0;
        }

//        Collections.swap(finalList, 0, playPosition);

        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(this, finalList.get(playPosition).getId());

        if (!mediaPlayer.isPlaying()) {
            Log.d(TAG, Thread.currentThread().getName());
            executor.execute(timeUpdaterRunnable);
        }

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
                    currentSeekBarPoss = mediaPlayer.getCurrentPosition();
                    Log.d(TAG, "run: " + this.toString() + " " + currentSeekBarPoss);
                    sendMessage();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            if (mediaPlayer.getCurrentPosition() / 1000 >= mediaPlayer.getDuration() / 1000) {
                next();
                Log.d(TAG, "run: " + "NEXT() " + " " + currentSeekBarPoss);
            }

        }
    };


    private void sendFinalList() {

        Intent intent = new Intent("song_list");
        intent.putParcelableArrayListExtra("finallist", (ArrayList<? extends Parcelable>) finalList);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("sendFinalList() ", "Broadcasting message");
    }


    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("messageCurrentPosition", String.valueOf(currentSeekBarPoss));
        intent.putExtra("messageMax", String.valueOf(mediaPlayer.getDuration()));
        Log.d("receiver", "Got intent.putExtra messageCurrent: " + currentSeekBarPoss + " ");

        intent.putExtra("current_song", playPosition);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "sendMessage: LocalBroadcastManager");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        currentSeekBarPoss = 0;
        sendMessage();
        mediaPlayer.stop();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}