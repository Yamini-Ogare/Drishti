package ognora.drishti;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

public class MyCountDown extends CountDownTimer
{
    private TextView textView1;
    MediaPlayer mp;

    public MyCountDown(long millisInFuture, long countDownInterval, TextView txtV, MediaPlayer m) {
        super(millisInFuture, countDownInterval);
        this.textView1 = txtV;
        this.mp = m;
        start();
    }

    @Override
    public void onTick(long l) {

    }

    @Override
    public void onFinish() {
        textView1.setVisibility(View.INVISIBLE);
        mp.stop();
    }

}
