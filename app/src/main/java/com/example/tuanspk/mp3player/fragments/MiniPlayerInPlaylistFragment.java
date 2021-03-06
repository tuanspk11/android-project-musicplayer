package com.example.tuanspk.mp3player.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.activities.PlaylistActivity;

public class MiniPlayerInPlaylistFragment extends Fragment {

    private ProgressBar progressBarSong;
    private TextView txtSongTitle;
    private TextView txtSongArtist;
    private ImageButton btnPlayPause;

    private boolean isPause;

    public ProgressBar getProgressBarSong() {
        return progressBarSong;
    }

    public void setTxtSongTitle(String txtSongTitle) {
        this.txtSongTitle.setText(txtSongTitle);
    }

    public void setTxtSongArtist(String txtSongArtist) {
        this.txtSongArtist.setText(txtSongArtist);
    }

    public ImageButton getBtnPlayPause() {
        return btnPlayPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;

        if (isPause)
            btnPlayPause.setBackgroundResource(R.drawable.ic_mini_play);
        else
            btnPlayPause.setBackgroundResource(R.drawable.ic_mini_pause);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        declare(view);
        init();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("on click", "mini controller");
                ((PlaylistActivity) getActivity()).songPlaying();
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPause) {
                    ((PlaylistActivity) getActivity()).start();
                    btnPlayPause.setBackgroundResource(R.drawable.ic_mini_pause);
                    isPause = false;
                } else {
                    ((PlaylistActivity) getActivity()).pause();
                    btnPlayPause.setBackgroundResource(R.drawable.ic_mini_play);
                    isPause = true;
                }
            }
        });

        return view;
    }

    private void declare(View view) {
        txtSongTitle = (TextView) view.findViewById(R.id.textview_song_title);
        txtSongArtist = (TextView) view.findViewById(R.id.textview_song_artist);
        progressBarSong = (ProgressBar) view.findViewById(R.id.progressbar_song_duration);
        btnPlayPause = (ImageButton) view.findViewById(R.id.play_pause_button);
    }

    private void init() {
        isPause = false;
    }

    public void show(View view) {
        view.setVisibility(view.VISIBLE);
    }

    public void hide(View view) {
        view.setVisibility(view.GONE);
    }
}
