package com.reigot.gameapppractice_animation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.Guideline;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //画面部品
    private ImageView _zoukinGirl;
    private TextView _tvZoukinY;

    //サウンド
    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private SparseIntArray soundIdMap = new SparseIntArray();

    //タイマー
    private Timer jumpTimer;
    private Handler mHandler = new Handler();

    //デバイス情報
    private float scale;
    private int screenWidth;
    private int screenHeight;

    //雑巾少女の配置
    private float _zoukinGirlY;
    private float _zoukinGirlYPrev;
    private float _zoukinGirlYDefault;

    //ジャンプ処理
    private boolean isJumping;
    private boolean isSecondJumping;
    private int delay;
    private float jumpPower;

    //アニメーション
    private AnimationDrawable animation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scale = getResources().getDisplayMetrics().density;
        DisplayMetrics dMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        screenWidth = dMetrics.widthPixels;
        screenHeight = dMetrics.heightPixels;

        //BGMの再生。
        playBGM(R.raw.main);

        //SEの再生準備。
        //入力したパスはsoundIdMapのkeyとして登録される。（可変長引数）
        prepareSE(R.raw.jump);


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        _tvZoukinY = findViewById(R.id.tvMain_zoukinY);
        _zoukinGirl = findViewById(R.id.zoukinGirl);

        animationPrepare();
    }

    public void playBGM(int bgmFilePath) {
        mediaPlayer = MediaPlayer.create(this, bgmFilePath);
        mediaPlayer.start();
    }

    public void prepareSE(int ... seFilePath) {
        //サウンドプールの用意
        SoundPool.Builder builder = new SoundPool.Builder().setMaxStreams(1);
        soundPool = builder.build();

        //soundIdMapに  (ファイルパス, 対応するsoundId)  を入れている。
        ArrayList<Integer> soundIdList = new ArrayList<>();
        for (int index = 0 ; index < seFilePath.length ; index++) {
            soundIdList.add(soundPool.load(MainActivity.this, seFilePath[index], 1));
            soundIdMap.append(seFilePath[index], soundIdList.get(index));
        }
    }

    public void animationPrepare() {
        _zoukinGirl.setBackgroundResource(R.drawable.zoukin_girl_list);
        animation = (AnimationDrawable) _zoukinGirl.getBackground();
        animation.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            jumpJudge();
        }

        return super.onTouchEvent(event);
    }

    public void jumpJudge() {




        if (!(isJumping)) {
            soundPool.play(soundIdMap.get(R.raw.jump), 1.0f, 1.0f, 0, 0, 1.0f);
            _zoukinGirlYDefault = _zoukinGirlY = _zoukinGirl.getY();
            delay = 0;
            jumpPower = 4.0f * scale + 15.0f;
            jumpAction();
        } else if (!(isSecondJumping)) {
            soundPool.play(soundIdMap.get(R.raw.jump), 1.0f, 1.0f, 0, 0, 1.0f);
            isSecondJumping = true;
            delay = 30;
            jumpPower = 4.0f * scale + 16.0f;
            jumpTimer.cancel();
            jumpAction();
        }

    }

    public void jumpAction() {
        _zoukinGirl.setBackgroundResource(R.drawable.zoukin_girl_jump0);
        _zoukinGirlYPrev = _zoukinGirlY;
        _zoukinGirlY -= jumpPower;
        _zoukinGirl.setY(_zoukinGirlY);
        jumpTimer = new Timer();
        TimerTask jumpTimerTask = new JumpTimerTask();
        jumpTimer.schedule(jumpTimerTask, delay, 9);
    }

    public class JumpTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    _tvZoukinY.setText(String.valueOf(_zoukinGirlY));
                    isJumping = true;
                    float y_temp = _zoukinGirlY;
                    _zoukinGirlY += (_zoukinGirlY - _zoukinGirlYPrev) + 1;
                    _zoukinGirlYPrev = y_temp;
                    _zoukinGirl.setY(_zoukinGirlY);

                    if(_zoukinGirlY > _zoukinGirlYDefault) {
                        _zoukinGirlY = _zoukinGirlYDefault;
                        _zoukinGirl.setY(_zoukinGirlY);
                        isJumping = false;
                        isSecondJumping = false;
                        animationPrepare();
                        jumpTimer.cancel();
                    }
                }
            });

        }
    }
}
