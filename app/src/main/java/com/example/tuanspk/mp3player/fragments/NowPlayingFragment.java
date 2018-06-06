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
import com.example.tuanspk.mp3player.visualizers.CircleBarVisualizer;

public class NowPlayingFragment extends Fragment implements View.OnClickListener {

//    private long songID;
//    private String lyrics;

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

        if (isPause)
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);
        else
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
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

    public void loadLyrics() {
//        String filename = getRealPathFromURI(Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + songID));
//        if (filename != null && lyrics == null) {
//            lyrics = LyricsExtractor.getLyrics(new File(filename));
//        }
//
//        if (lyrics != null) {
//            txtSongLyric.setText(lyrics);
//        } else {
//            String artist = txtSongArtist.getText().toString();
//            if (artist != null) {
//                int i = artist.lastIndexOf(" feat");
//                if (i != -1) {
//                    artist = artist.substring(0, i);
//                }
//
//                LyricsLoader.getInstance(((MainActivity) getActivity()).getApplicationContext()).getLyrics(
//                        artist, txtSongTitle.getText().toString(), new Callback<String>() {
//                            @Override
//                            public void success(String s, Response response) {
//                                lyrics = s;
//                                if (s.equals("Sorry, We don't have lyrics for this song yet.\n")) {
//                                    txtSongLyric.setText(R.string.no_lyrics);
//                                } else {
//                                    txtSongLyric.setText(s);
//                                    txtLyricPoweredby.setVisibility(View.VISIBLE);
//                                }
//                            }
//
//                            @Override
//                            public void failure(RetrofitError error) {
//                                txtSongLyric.setText(R.string.no_lyrics);
//                            }
//                        });
//
//            } else {
//                txtSongLyric.setText(R.string.no_lyrics);
//            }
//        }
    }

//    private String getRealPathFromURI(Uri contentUri) {
//        String[] proj = {MediaStore.Audio.Media.DATA};
//        CursorLoader loader = new CursorLoader(((MainActivity) getActivity()).getApplicationContext(),
//                contentUri, proj, null, null, null);
//        Cursor cursor = loader.loadInBackground();
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
//        cursor.moveToFirst();
//        String result = cursor.getString(column_index);
//        cursor.close();
//        return result;
//    }

    public void setCircleBarVisualizer(int audioSessionID) {
        circleBarVisualizer.setColor(ContextCompat.getColor(
                ((MainActivity) getActivity()).getApplicationContext(), R.color.colorPink));
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
//        lyrics = null;

        animation = AnimationUtils.loadAnimation(
                ((MainActivity) getActivity()).getApplicationContext(), R.anim.disc_rotate);
        imgDisc.startAnimation(animation);
    }

}
