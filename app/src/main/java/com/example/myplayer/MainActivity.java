package com.example.myplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


public class MainActivity extends AppCompatActivity implements FragmentCommunicator1 {


    private static final String TAG = "My_LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FragmentManager fm = getSupportFragmentManager();

        SectionsPagerAdapter pagerAdapter =
                new SectionsPagerAdapter(fm, 1);
        ViewPager pager = findViewById(R.id.pager);

        pager.setAdapter(pagerAdapter);


        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);


    }

    @Override
    public void onActivityCallback1(int song) {

        Intent intent = new Intent(this, MyService.class);
        intent.setAction("custom_song");
        intent.putExtra("currentPosition", song);
        startService(intent);

        Log.d(TAG, "LocalBroadcastManagerNumberOfSong" + song);
    }

}
