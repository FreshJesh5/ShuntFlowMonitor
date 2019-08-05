package no.nordicsemi.android.nrftoolbox;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;

public class TimerActivity extends AppCompatActivity {

  //  private long startTime = 0L;
    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_timer);
      mProgressBar = (ProgressBar) findViewById(R.id.timerProgressBar);
      myTimer.start();
    }

    CountDownTimer myTimer = new CountDownTimer(120000,1000) {

        public void onTick(long millisUntilFinished) {
            //  sets the progress bar progress to millisUntilFinished/1000
            int myInt = (int) millisUntilFinished/1000;
            mProgressBar.setProgress(myInt);
            Long elapSeconds = 0L;
            Long secondDisp = 0L;
            Long minuteDisp = 0L;
            String Timer = null;
            elapSeconds = millisUntilFinished/1000;
            secondDisp = elapSeconds % 60;
            minuteDisp = elapSeconds / 60;

            if (minuteDisp < 10L)
                Timer = "0";
            Timer += Long.toString(minuteDisp) + ":";
            if (secondDisp < 10L)
                Timer += "0";
            Timer += Long.toString(secondDisp);
            TextView clock = (TextView) findViewById(R.id.clock);
            clock.setText(Timer);
        }
        public void onFinish() {
            //TO DO
        }
    };

    public void updateTimer(android.view.View view){
    }
}
