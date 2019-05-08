package com.example.maps;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class Music extends Service {
    private MediaPlayer mediaPlayer;

    public Music() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        Log.e(MainActivity.class.getSimpleName(),"Music");

        // Tạo đối tượng MediaPlayer, chơi file nhạc của bạn.
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // Chơi nhạc.
        mediaPlayer.start();

        return START_STICKY;
    }

    // Hủy bỏ dịch vụ.
    @Override
    public void onDestroy() {
        // Giải phóng nguồn dữ nguồn phát nhạc.
        mediaPlayer.release();
        super.onDestroy();
    }
}