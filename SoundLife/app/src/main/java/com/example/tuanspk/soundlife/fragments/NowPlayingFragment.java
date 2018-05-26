package com.example.tuanspk.soundlife.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.activities.MainActivity;

public class NowPlayingFragment extends Fragment implements View.OnClickListener {

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

    public String getTxtSongTitle() {
        return txtSongTitle.getText().toString();
    }

    public void setTxtSongTitle(String txtSongTitle) {
        this.txtSongTitle.setText(txtSongTitle);
    }

    public String getTxtSongArtist() {
        return txtSongArtist.getText().toString();
    }

    public void setTxtSongArtist(String txtSongArtist) {
        this.txtSongArtist.setText(txtSongArtist);
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    public String getTxtSongDuration() {
        return txtSongDuration.getText().toString();
    }

    public void setTxtSongDuration(String txtSongDuration) {
        this.txtSongDuration.setText(txtSongDuration);
    }

    public String getTxtSongElapedTime() {
        return txtSongElapedTime.getText().toString();
    }

    public void setTxtSongElapedTime(String txtSongElapedTime) {
        this.txtSongElapedTime.setText(txtSongElapedTime);
    }

    public ImageButton getBtnPlayPause() {
        return btnPlayPause;
    }

    public void setBtnPlayPause(ImageButton btnPlayPause) {
        this.btnPlayPause = btnPlayPause;
    }

    public ImageView getBtnShuffle() {
        return btnShuffle;
    }

    public void setBtnShuffle(ImageView btnShuffle) {
        this.btnShuffle = btnShuffle;
    }

    public ImageView getBtnRepeat() {
        return btnRepeat;
    }

    public void setBtnRepeat(ImageView btnRepeat) {
        this.btnRepeat = btnRepeat;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public int getRepeat() {
        return repeat;
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
                ((MainActivity) getActivity()).setOnSeekBarChange(progress);
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
                ((MainActivity) getActivity()).previous();
                break;
            case R.id.play_pause_button:
                if (isPause) {
                    ((MainActivity) getActivity()).start();
                    btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
                    isPause = false;
                } else {
                    ((MainActivity) getActivity()).pause();
                    btnPlayPause.setBackgroundResource(R.drawable.ic_play);
                    isPause = true;
                }
                break;
            case R.id.button_next:
                ((MainActivity) getActivity()).next();
                break;
            case R.id.button_shuffle:
                ((MainActivity) getActivity()).setShuffleClicked();
                break;
            case R.id.button_repeat:
                ((MainActivity) getActivity()).setRepeatClicked();
                break;
        }
    }

    public void show(View view) {
        view.setVisibility(view.VISIBLE);
    }

    public void hide(View view) {
        view.setVisibility(view.GONE);
    }

    private void declare(View view) {
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
    }
}
