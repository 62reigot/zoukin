package com.reigot.gameapppractice_animation;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //画面部品
    private ImageView ivClothGirl;
    private TextView tvClothGirlY;

    //サウンド
    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private SparseIntArray soundIdMap = new SparseIntArray();

    //タイマー
    private Timer jumpTimer;
    private Handler jumpHandler = new Handler();

    //デバイス情報
    private float scale;

    //雑巾少女の配置
    private float clothGirlY;
    private float clothGirlPrev;
    private float ClothGirlDefaultY;

    //ジャンプ処理
    private JumpingPhase jumpingPhase = JumpingPhase.standby;
    private int delay;
    private float jumpingPower;

    public static class MagicNumberManager {
        //BGMの音量
        static final float MAIN_BGM_VOLUME = 0.8f;

        //ジャンプ関連の定数
        static final float JUMP_SE_VOLUME = 0.2f;
        static final float JUMP_SE_RATE = 1.0f;
        static final int FIRST_JUMPING_DELAY = 0;
        static final int SECOND_JUMPING_DELAY = 30;
        static final int JUMP_TIMER_TASK_PERIOD = 9;
        static final float JUMPING_POWER_COEFFICIENT = 5.1f;
        static final float FIRST_JUMPING_POWER_BASE = 13.0f;
        static final float SECOND_JUMPING_POWER_BASE = 12.0f;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //画面サイズによってジャンプ力を調整するための変数を用意。
        scale = getResources().getDisplayMetrics().density;

        //ステータスバーを非表示に変更。
        hideStatusBar();

        //画面部品の取得。
        findViewContent();

        //Gif動画の適用。
        loadGifAnimation();

        //BGMの再生。
        playBgm(R.raw.main);

        //SEの再生準備。
        //入力したパスはsoundIdMapのkeyとして登録される。（可変長引数）
        prepareSe(R.raw.jump);

        //TO DO: 障害物（机）を準備。
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //タッチされたらジャンプ処理へ進む。
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            judgeJumpingSituation();
        }
        return super.onTouchEvent(event);
    }

    public void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void findViewContent() {
        tvClothGirlY = findViewById(R.id.tvMain_zoukinY);
        ivClothGirl = findViewById(R.id.zoukinGirl);
    }

    public void loadGifAnimation() {
        Glide.with(this).load(R.raw.zoukin_girl_run).into(ivClothGirl);
    }

    public void playBgm(int bgmFilePath) {
        mediaPlayer = MediaPlayer.create(this, bgmFilePath);
        mediaPlayer.setVolume(MagicNumberManager.MAIN_BGM_VOLUME, MagicNumberManager.MAIN_BGM_VOLUME);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void prepareSe(int ... seFilePath) {
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

    public void playSe(int seFilePath, float volume, float rate) {
        soundPool.play(soundIdMap.get(seFilePath), volume, volume, 0, 0, rate);
    }

    enum JumpingPhase {
        standby,
        firstJump,
        secondJump
    }
    public void judgeJumpingSituation() {
        final float firstJumpingPower = scale * MagicNumberManager.JUMPING_POWER_COEFFICIENT + MagicNumberManager.FIRST_JUMPING_POWER_BASE;
        final float secondJumpingPower = scale * MagicNumberManager.JUMPING_POWER_COEFFICIENT + MagicNumberManager.SECOND_JUMPING_POWER_BASE;

        switch (jumpingPhase) {
            case standby: //ジャンプ中でなければ1段目のジャンプをする。
                jumpingPhase = JumpingPhase.firstJump;
                playSe(R.raw.jump, MagicNumberManager.JUMP_SE_VOLUME, MagicNumberManager.JUMP_SE_RATE);
                ClothGirlDefaultY = clothGirlY = ivClothGirl.getY();
                delay = MagicNumberManager.FIRST_JUMPING_DELAY;
                jumpingPower = firstJumpingPower;
                startJumping();
                break;
            case firstJump: //1段目のジャンプ中ならば2段目のジャンプをする。
                jumpingPhase = JumpingPhase.secondJump;
                playSe(R.raw.jump, MagicNumberManager.JUMP_SE_VOLUME, MagicNumberManager.JUMP_SE_RATE);
                delay = MagicNumberManager.SECOND_JUMPING_DELAY;
                jumpingPower = secondJumpingPower;
                jumpTimer.cancel();
                startJumping();
                break;
            case secondJump: //二段目のジャンプ中には何も処理をしない。（今後処理を追加する可能性あり）
                break;
        }
    }

    public void startJumping() {
        Glide.with(this).load(R.drawable.zoukin_girl_jump0).into(ivClothGirl);
        clothGirlPrev = clothGirlY;
        clothGirlY -= jumpingPower;
        ivClothGirl.setY(clothGirlY);
        jumpTimer = new Timer();
        TimerTask jumpTimerTask = new JumpingTimerTask();
        jumpTimer.schedule(jumpTimerTask, delay, MagicNumberManager.JUMP_TIMER_TASK_PERIOD);
    }

    public class JumpingTimerTask extends TimerTask {
        @Override
        public void run() {
            jumpHandler.post(new Runnable() {
                @Override
                public void run() {
                    //自然なジャンプをするための構文
                    tvClothGirlY.setText(String.valueOf(clothGirlY));
                    float y_temp = clothGirlY;
                    clothGirlY += (clothGirlY - clothGirlPrev) + 1;
                    clothGirlPrev = y_temp;
                    ivClothGirl.setY(clothGirlY);


                    if(clothGirlY > ClothGirlDefaultY) {
                        //キャラクターが元の高さに戻ったらジャンプアクションを終了する。
                        clothGirlY = ClothGirlDefaultY;
                        ivClothGirl.setY(clothGirlY);
                        jumpingPhase = JumpingPhase.standby;
                        loadGifAnimation();
                        jumpTimer.cancel();
                    }
                }
            });

        }
    }
}
