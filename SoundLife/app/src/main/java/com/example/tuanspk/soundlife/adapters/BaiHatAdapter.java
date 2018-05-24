package com.example.tuanspk.soundlife.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.models.BaiHat;

import java.util.ArrayList;

public class BaiHatAdapter extends BaseAdapter {
    Context context;
    int layout;
    private ArrayList<BaiHat> listBaiHat;

    public BaiHatAdapter(Context context, int layout, ArrayList<BaiHat> listBaiHat) {
        this.context = context;
        this.listBaiHat = listBaiHat;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return listBaiHat.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView title;
        TextView artist;
        TextView duration;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(layout, null);
            viewHolder.title = convertView.findViewById(R.id.textview_song_title);
            viewHolder.artist = convertView.findViewById(R.id.textview_song_artist);
            viewHolder.duration = convertView.findViewById(R.id.textview_song_duration);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.title.setText(listBaiHat.get(position).getTitle());
        int minute = listBaiHat.get(position).getDuration() / 60000;
        int second = listBaiHat.get(position).getDuration() / 1000 % 60;
        String secondValue = String.valueOf(second);
        if (second < 10) secondValue = "0" + secondValue;
        viewHolder.duration.setText(minute + ":" + secondValue);
        viewHolder.artist.setText(listBaiHat.get(position).getArtist());

        return convertView;
    }

    public ArrayList<BaiHat> getListBaiHat() {
        return listBaiHat;
    }
}
