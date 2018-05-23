package com.example.tuanspk.soundlife.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tuanspk.soundlife.R;

public class MiniPlayerFragment extends Fragment {

    RelativeLayout view;

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

        view = (RelativeLayout) inflater.inflate(R.layout.fragment_mini_player, container, false);
        declare();
        return view;
    }

    private void declare() {
        txtSongTitle = (TextView) view.findViewById(R.id.textview_song_title);
        txtSongArtist = (TextView) view.findViewById(R.id.textview_song_artist);
        progressBarSong = (ProgressBar) view.findViewById(R.id.song_progress_normal);
        btnPlayPause = (ImageButton) view.findViewById(R.id.play_pause_button);
    }
}
