package ognora.drishti;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

public class MyCountDown extends CountDownTimer
{
    private TextView textView1;

    public MyCountDown(long millisInFuture, long countDownInterval, TextView txtV) {
        super(millisInFuture, countDownInterval);
        // TODO Auto-generated constructor stub
        this.textView1 = txtV;
        start();
    }

    @Override
    public void onTick(long l) {

    }

    @Override
    public void onFinish() {
        textView1.setVisibility(View.INVISIBLE);
    }

}
