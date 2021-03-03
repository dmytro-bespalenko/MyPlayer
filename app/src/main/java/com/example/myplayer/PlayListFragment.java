package com.example.myplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayListFragment extends Fragment implements AdapterCommunicator {


    private RecycleAdapter recycleAdapter;
    private List<Playlist> playList;

    private final String TAG = "My_Log";
    private FragmentCommunicator fragmentCommunicator;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_list, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentCommunicator = (FragmentCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException();
        }

    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.playlist_recycle_view);
        try {
            getList();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(mMessageReceiver,
                new IntentFilter("song_list"));

        recycleAdapter = new RecycleAdapter(playList);
        recycleAdapter.registerListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(recycleAdapter);


        Intent intent = new Intent(getContext(), MyService.class);
        intent.putParcelableArrayListExtra("song", (ArrayList<? extends Parcelable>) playList);
        intent.setAction("LIST");
        Objects.requireNonNull(getActivity()).startService(intent);


    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Playlist> finalList = intent.getParcelableArrayListExtra("finallist");
            Log.d(TAG, "finallist: " + intent.getIntegerArrayListExtra("finallist"));

            recycleAdapter.setSongsList(finalList);
            recycleAdapter.notifyDataSetChanged();

        }
    };

    public void getList() throws IllegalAccessException {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Field[] fields = R.raw.class.getFields();


        playList = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {

            Uri mediaPath = Uri.parse("android.resource://" + Objects.requireNonNull(getActivity()).getPackageName() + "/" + fields[i].getInt(fields[i]));

            mmr.setDataSource(getActivity(), mediaPath);

            String sponsorTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String sponsorArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            Log.d(TAG, "getList: " + i);
            playList.add(new Playlist(sponsorTitle, sponsorArtist, fields[i].getInt(fields[i])));
            Log.d("Integer.parseInt ", "Integer.parseInt(fields[i].toString()) " + fields[i].getInt(fields[i]));

        }
        Log.d(TAG, "listRaw: " + playList.toString());

    }


    @Override
    public void onItemClicked(int position) {
        fragmentCommunicator.onActivityClickBack(position);
    }

    @Override
    public void onItemLongClicked(int position) {
        fragmentCommunicator.onActivityLongClickBack(position);

    }


}