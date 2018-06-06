package com.example.tuanspk.mp3player.utils;

import android.content.Context;
import android.widget.Toast;

public class ShowToasFunctions {

    public void showToastLengthShort(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showToastLengthLong(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

}
