package com.example.tuanspk.soundlife.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.activities.MainActivity;

public class MiniPlayerFragment extends Fragment {

    private ProgressBar progressBarSong;
    private TextView txtSongTitle;
    private TextView txtSongArtist;
    private ImageButton btnPlayPause;

    public void setTxtSongTitle(String txtSongTitle) {
        this.txtSongTitle.setText(txtSongTitle);
    }

    public void setTxtSongArtist(String txtSongArtist) {
        this.txtSongArtist.setText(txtSongArtist);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        declare(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("on click", "mini controller");
                ((MainActivity) getActivity()).songPlaying();
            }
        });
        return view;
    }

    private void declare(View view) {
        txtSongTitle = (TextView) view.findViewById(R.id.textview_song_title);
        txtSongArtist = (TextView) view.findViewById(R.id.textview_song_artist);
        progressBarSong = (ProgressBar) view.findViewById(R.id.song_progress_normal);
        btnPlayPause = (ImageButton) view.findViewById(R.id.play_pause_button);
    }

    public void show(View view) {
        view.setVisibility(view.VISIBLE);
    }

    public void hide(View view) {
        view.setVisibility(view.GONE);
    }
}
