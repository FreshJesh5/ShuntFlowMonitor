package no.nordicsemi.android.nrftoolbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TimerActivity extends AppCompatActivity {

    private long startTime = 0L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
    }

    public void updateTimer(android.view.View view){
        Long elapTime = 0L;
        Long elapSeconds = 0L;
        Long secondDisp = 0L;
        Long minuteDisp = 0L;
        String Timer = null;

        if(startTime==0L) {
            //initialize timer
            startTime=System.currentTimeMillis()+120000L;
        }
        else {
            //update timer values
            elapTime = startTime - System.currentTimeMillis();
            elapSeconds = elapTime / 1000;
            secondDisp = elapSeconds % 60;
            minuteDisp = elapSeconds / 60;
            ///set up timer display
            if(elapTime <=0L) {
                //make "next step active

            }
            else {
                if (minuteDisp < 10L)
                    Timer = "0";
                Timer += Long.toString(minuteDisp) + ":";
                if (secondDisp < 10L)
                    Timer += "0";
                Timer += Long.toString(secondDisp);
                TextView clock = (TextView) findViewById(R.id.clock);
                clock.setText(Timer);
            }
        }
    }
}
