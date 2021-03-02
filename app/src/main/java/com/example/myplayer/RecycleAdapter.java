package com.example.myplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {
    private final String TAG = "My_Log";

    private List<Playlist> songsList;
    private AdapterCommunicator adapterCommunicator;


    public void setSongsList(List<Playlist> songsList) {
        this.songsList = songsList;
    }

    public RecycleAdapter(List<Playlist> songsList) {
        this.songsList = songsList;

    }

    public void registerListener(AdapterCommunicator adapterCommunicator) {
        this.adapterCommunicator = adapterCommunicator;
    }

    @NonNull
    @Override
    public RecycleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        return new ViewHolder((CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_music, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Playlist song = songsList.get(position);

        CardView cv = holder.cardView;

        TextView nameView = cv.findViewById(R.id.nameMusic);
        nameView.setText(song.getName());

        TextView artistView = cv.findViewById(R.id.artistMusic);
        artistView.setText(String.valueOf(song.getArtist()));


    }


    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;
        final TextView nameView;
        final TextView artistView;
        final ImageView imageView;

        public ViewHolder(@NonNull CardView itemView) {
            super(itemView);
            cardView = itemView;
            nameView = itemView.findViewById(R.id.nameMusic);
            artistView = itemView.findViewById(R.id.artistMusic);
            imageView = itemView.findViewById(R.id.iconMusic);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    adapterCommunicator.onItemClicked(getAdapterPosition());

                }
            });
        }


    }

}
