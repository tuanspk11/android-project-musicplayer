package com.example.tuanspk.mp3player.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class IOInternalStorage extends Activity {

    public File readInternal(Context context, String filePath, String fileName) {
        File internalFile;
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir(filePath, Context.MODE_PRIVATE);
        internalFile = new File(directory, fileName);

        return internalFile;
    }

    public String[] readFileInInternal(File file, String filePath, String fileName) {
        String[] arrayList = null;

//        try {
//            FileInputStream inputStream = openFileInput(fileName);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            arrayList = new String[999];
//            String s = "";
//            int i = 0;
//            while ((s = reader.readLine()) != null) {
//                arrayList[i] = s.toString();
//                i++;
//            }
//            inputStream.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            // đọc file
            FileInputStream inputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            String strLine;
            // lấy độ dài của list
            int i = 0;
            while ((strLine = bufferedReader.readLine()) != null) {
                i++;
            }
            arrayList = new String[i];
            // đọc từng dòng
            inputStream = new FileInputStream(file);
            dataInputStream = new DataInputStream(inputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            i = 0;
            while ((strLine = bufferedReader.readLine()) != null) {
                arrayList[i] = strLine;
                i++;
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public void writeFileInInternal(File file, String[] list) {
        try {
//            FileOutputStream outputStream = openFileOutput("playlist", 0);
//            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
//            for (int i = 0; i < listPlaylistName.length; i++)
//                writer.write(listPlaylistName[i] + "\n");
//            writer.close();

            FileOutputStream outputStream = new FileOutputStream(file);
            for (int i = 0; i < list.length; i++)
                outputStream.write((list[i] + "\n").getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
