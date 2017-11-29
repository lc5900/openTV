package com.lc5900.tv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lc5900.liuchun.tv.R;
import com.lc5900.tv.utils.PlayerManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InternetVideoDemo extends AppCompatActivity implements PlayerManager.PlayerStateListener {
    private PlayerManager player;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_internet_video_demo);
        ButterKnife.bind(this);

        initPlayer();


    }

    private void initPlayer() {

        player = new PlayerManager(this);
        player.setFullScreenOnly(true);
        player.setScaleType(PlayerManager.SCALETYPE_WRAPCONTENT);
        player.playInFullScreen(true);
        player.setPlayerStateListener(this);
        progressBar.setVisibility(View.VISIBLE);
        player.play(getIntent().getStringExtra("url"));

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (player.gestureDetector.onTouchEvent(event))
            return true;
        return super.onTouchEvent(event);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlay() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        player.stop();
        super.onDestroy();
    }
}
