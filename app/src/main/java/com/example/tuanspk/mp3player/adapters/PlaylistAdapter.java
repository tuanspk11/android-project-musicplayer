package com.example.tuanspk.mp3player.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.callbacks.IPlaylistAdapterCallbacks;
import com.example.tuanspk.mp3player.models.Playlist;

import java.util.ArrayList;

public class PlaylistAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Playlist> listPlaylist;
    private LayoutInflater songInflater;

    private IPlaylistAdapterCallbacks playlistAdapterCallbacks;

    public void setPlaylistAdapterCallbacks(IPlaylistAdapterCallbacks callbacks) {
        playlistAdapterCallbacks = callbacks;
    }

    public PlaylistAdapter(Context context, ArrayList<Playlist> listPlaylist) {
        this.context = context;
        this.listPlaylist = listPlaylist;
        this.songInflater = LayoutInflater.from(context);
    }

    public ArrayList<Playlist> getListPlaylist() {
        return listPlaylist;
    }

    public void setListPlaylist(ArrayList<Playlist> listPlaylist) {
        this.listPlaylist = listPlaylist;
    }

    @Override
    public int getCount() {
        return listPlaylist.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // map to playlist layout
        RelativeLayout playlistLayout = (RelativeLayout) songInflater.inflate(
                R.layout.item_playlist, parent, false);

        // declare name view and set button option
        TextView txtName = (TextView) playlistLayout.findViewById(R.id.textview_playlist_name);
        ImageButton buttonOption = playlistLayout.findViewById(R.id.button_playlist_option);
        buttonOption.setTag(getItem(position));
        buttonOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, position);
                Log.e("playlist position", String.valueOf(position));
            }
        });

        // get playlist using postion
        Playlist currentPlaylist = listPlaylist.get(position);

        // get name string
        txtName.setText(currentPlaylist.getName());

        // set position as tag
        playlistLayout.setTag(position);

        return playlistLayout;
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenuInflater().inflate(R.menu.menu_item_playlist, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_rename:
                        return true;
                    case R.id.item_delete:
                        deletePlaylist(listPlaylist.get(position).getName());
                        if (playlistAdapterCallbacks != null)
                            playlistAdapterCallbacks.eventItemClick(listPlaylist);
                        return true;
                }
                return true;
            }
        });
        menu.show();
    }

    public void deletePlaylist(String playlistName) {
        for (int i = 0; i < listPlaylist.size(); i++) {
            if (listPlaylist.get(i).getName().equals(playlistName.toString().trim()))
                listPlaylist.remove(listPlaylist.get(i));
        }
    }
}
