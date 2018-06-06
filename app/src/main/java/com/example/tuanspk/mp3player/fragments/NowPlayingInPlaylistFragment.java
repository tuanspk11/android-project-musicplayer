package com.example.tuanspk.mp3player.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.activities.MainActivity;
import com.example.tuanspk.mp3player.activities.PlaylistActivity;
import com.example.tuanspk.mp3player.visualizers.CircleBarVisualizer;

public class NowPlayingInPlaylistFragment extends Fragment implements View.OnClickListener {

    private CircleBarVisualizer circleBarVisualizer;
    private ImageView imgDisc;

    private TextView txtSongTitle;
    private TextView txtSongArtist;
    private SeekBar seekBar;
    private TextView txtSongDuration;
    private TextView txtSongElapedTime;
    private ImageButton btnPrevious;
    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    private ImageView btnShuffle;
    private ImageView btnRepeat;

    private boolean isPause;
    private boolean isShuffle;
    private int repeat;

    private Animation animation;

    public void setTxtSongTitle(String txtSongTitle) {
        this.txtSongTitle.setText(txtSongTitle);
    }

    public void setTxtSongArtist(String txtSongArtist) {
        this.txtSongArtist.setText(txtSongArtist);
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public void setTxtSongDuration(String txtSongDuration) {
        this.txtSongDuration.setText(txtSongDuration);
    }

    public void setTxtSongElapedTime(String txtSongElapedTime) {
        this.txtSongElapedTime.setText(txtSongElapedTime);
    }

    public ImageButton getBtnPlayPause() {
        return btnPlayPause;
    }

    public ImageView getBtnShuffle() {
        return btnShuffle;
    }

    public ImageView getBtnRepeat() {
        return btnRepeat;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_nowplaying, container, false);
        declare(view);
        init();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((PlaylistActivity) getActivity()).setOnSeekBarChange(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnPrevious.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_previous:
                ((PlaylistActivity) getActivity()).previous();
                break;
            case R.id.play_pause_button:
                if (isPause) {
                    ((PlaylistActivity) getActivity()).start();
                    btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
                    isPause = false;
                } else {
                    ((PlaylistActivity) getActivity()).pause();
                    btnPlayPause.setBackgroundResource(R.drawable.ic_play);
                    isPause = true;
                }
                break;
            case R.id.button_next:
                ((PlaylistActivity) getActivity()).next();
                break;
            case R.id.button_shuffle:
                ((PlaylistActivity) getActivity()).setShuffleClicked();
                break;
            case R.id.button_repeat:
                ((PlaylistActivity) getActivity()).setRepeatClicked();
                break;
        }
    }

    public void setCircleBarVisualizer(int audioSessionID) {
        if (circleBarVisualizer.getVisualizer() != null)
            circleBarVisualizer.release();
        circleBarVisualizer.setColor(ContextCompat.getColor(
                ((PlaylistActivity) getActivity()).getApplicationContext(), R.color.colorPink));
        circleBarVisualizer.setPlayer(audioSessionID);
    }

    public void show(View view) {
        view.setVisibility(view.VISIBLE);
    }

    public void hide(View view) {
        view.setVisibility(view.GONE);
        circleBarVisualizer.release();
    }

    private void declare(View view) {
        circleBarVisualizer = view.findViewById(R.id.visualizer);
        imgDisc = view.findViewById(R.id.imageview_disc);

        txtSongTitle = view.findViewById(R.id.textview_song_title);
        txtSongArtist = view.findViewById(R.id.textview_song_artist);
        seekBar = view.findViewById((R.id.seekbar_song_duration));
        txtSongDuration = view.findViewById(R.id.textview_song_duration);
        txtSongElapedTime = view.findViewById(R.id.textview_song_elapsed_time);
        btnPrevious = view.findViewById(R.id.button_previous);
        btnPlayPause = view.findViewById(R.id.play_pause_button);
        btnNext = view.findViewById(R.id.button_next);
        btnShuffle = view.findViewById(R.id.button_shuffle);
        btnRepeat = view.findViewById(R.id.button_repeat);
    }

    private void init() {
        isPause = false;
        repeat = 0;

        animation = AnimationUtils.loadAnimation(
                ((PlaylistActivity) getActivity()).getApplicationContext(), R.anim.disc_rotate);
        imgDisc.startAnimation(animation);
    }

}
