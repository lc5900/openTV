package com.example.liuchun.tv;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.MediaController;
import android.widget.VideoView;

public class InternetVideoDemo extends Activity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_internet_video_demo);

        Uri uri = Uri.parse(getIntent().getStringExtra("url"));

        VideoView videoView = (VideoView)this.findViewById(R.id.video_view);
        videoView.setVideoURI(uri);
        //
        videoView.requestFocus();
        videoView.start();
    }
}
