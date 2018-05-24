package com.example.tuanspk.soundlife;

import com.example.tuanspk.soundlife.models.BaiHat;

import java.util.ArrayList;

public class ListSong {
    public ArrayList getList() {
        ArrayList<BaiHat> listMusic = new ArrayList<>();
        listMusic.add(new BaiHat(0, "Cho tôi xin một vé đi tuổi thơ", R.raw.cho_toi_xin_mot_ve_di_tuoi_tho, "Link Lee", 132000));
        listMusic.add(new BaiHat(1, "Đời dạy tôi", R.raw.doi_day_toi, "Only C", 94000));
        listMusic.add(new BaiHat(2, "Làm tình nguyện hết mình", R.raw.lam_tinh_nguyen_het_minh, "Ba Con Soi", 126000));
        listMusic.add(new BaiHat(3, "Mình yêu nhau đi", R.raw.minh_yeu_nhau_di, "Bích Phương", 109000));
        listMusic.add(new BaiHat(4, "Người âm phủ", R.raw.nguoi_am_phu, "I Dont Know", 56000));
        listMusic.add(new BaiHat(5, "Nơi này có anh", R.raw.noi_nay_co_anh, "Sơn Tùng MTP", 155000));
        listMusic.add(new BaiHat(6, "Yêu 5", R.raw.yeu_5, "I dont Know", 113000));
        return listMusic;
    }
}
