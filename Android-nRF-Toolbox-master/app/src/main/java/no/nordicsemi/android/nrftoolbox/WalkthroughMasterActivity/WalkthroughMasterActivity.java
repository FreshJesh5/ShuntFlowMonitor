package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.GraphicalView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid.LineGraphView;
import no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity.WalkthroughMaster_Manager;
import no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity.WalkthroughMaster_ManagerCallback;
import no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid.log;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;

public class WalkthroughMasterActivity extends BleProfileActivity
        implements TimerFragment.OnFragmentInteractionListener,
                   InstructionFragment.OnFragmentInteractionListener,
                    ContactTestFragment.OnFragmentInteractionListener,
                    WalkthroughMaster_ManagerCallback{

    private MyPagerAdapter pagerAdapter;
    private ViewPager vpPager;

    private final String TAG = "WT_MAIN_Activity";

    private final static String GRAPH_STATUS = "graph_status";
    private final static String GRAPH_COUNTER = "graph_counter";
    public final static int GRAPH_WINDOW = 2000;  //181015, org=10

    List<String> UID_List = new ArrayList<>();
    private String[] UID_Array = new String[4];
    private double[] batteryarray = new double[50];
    private int batcount = 0;

    private no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid.log datalog = new log();

    private Switch log;
    private Switch led;
    boolean NewLog = true;

    private WalkthroughMaster_Manager manager;

    private final static int REFRESH_INTERVAL = 100;//1 second interval
    //	private short axis = 0;


    private Handler mHandler = new Handler();

    private boolean isGraphInProgress = false;

    private GraphicalView mGraphView;
    private LineGraphView mLineGraph = new LineGraphView();

    private int mCounter = 0;

    private log logger = new log();

    private int[] ecg_hrs = new int[10];
    private int[] scg_hrs = new int[10];

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough_master);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupViewPager();
    }*/

    @Override
    protected void onCreateView(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_walkthrough_master);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //setGUI();
        setupViewPager();
        // Listener for Led/GPIO switch
        /*
        led.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                byte[] bleData = {0};

                if (isChecked) {
                    bleData[0] = 1;
                    manager.writeRXCharacteristic(bleData);
                    // Show the switch button checked status as toast message
                    Toast.makeText(getApplicationContext(),
                            "Heater is on", Toast.LENGTH_LONG).show();
                } else {
                    // If the switch button is off
                    // Show the switch button checked status as toast message
                    bleData[0] = 0;
                    manager.writeRXCharacteristic(bleData);
                    Toast.makeText(getApplicationContext(),
                            "Heater is off", Toast.LENGTH_LONG).show();
                }
            }
        });
        */
    }


    protected void onResume() {
        super.onResume();
    }


    private void setGUI() {
        log = (Switch) findViewById(R.id.switch1);
        led = (Switch) findViewById(R.id.led_switch);
        mLineGraph = mLineGraph.getLineGraphView();
        showGraph();
    }


    private void showGraph() {
        mGraphView = mLineGraph.getView(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.graph_temp1);
        layout.addView(mGraphView);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
            mCounter = savedInstanceState.getInt(GRAPH_COUNTER);

            if (isGraphInProgress)
                startShowGraph();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
        outState.putInt(GRAPH_COUNTER, mCounter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopShowGraph();
    }

    /*
        @Override
        protected int getLoggerProfileTitle() {
            return R.string.hrs_feature_title;
        }

        @Override
        protected int getAboutTextId() {
            return R.string.hrs_about_text;
        }

        @Override
        protected int getDefaultDeviceName() {
            return R.string.hrs_default_name;
        }
    */
    @Override
    protected UUID getFilterUUID() {
        return WalkthroughMaster_Manager.HR_SERVICE_UUID;
    }


    @Override
    protected int getLoggerProfileTitle() {
        return R.string.hrs_feature_title;
    }

    @Override
    protected int getAboutTextId() {
        return R.string.hrs_about_text;
    }

    @Override
    protected int getDefaultDeviceName() {
        return R.string.hrs_default_name;
    }



    void updateGraph(int[] x,int[] y, int[] z, int[] a) {

        for(int i = 0; i < 2; i++) {
            mCounter++;
            mLineGraph.addValue_x(mCounter, .0035*x[i]);
            mLineGraph.addValue_y(mCounter, .0035*y[i]);
            mLineGraph.addValue_z(mCounter, .0035*z[i]);
            mLineGraph.addValue_a(mCounter, .0035*a[i]);
            if (mCounter > GRAPH_WINDOW) {
                mLineGraph.removeold(0);

            }
//			mLineGraph2.addValue(mCounter, scg[0]);
//			if (mCounter > GRAPH_WINDOW) {
//				mLineGraph2.removeold(0);
//
//			}
        }
    }

    //added updateBattery function. This function takes the b array filled with advertised
    //battery life data and adds it to the mybattery TextView, which will display it. The
    //function only updates the View every 50 cycles to save computing work, and the conversion
    //equation used to transform the value in the b array to a percent was found experimentally,
    //and may be slightly off
    void updateBattery(int b) {
        double avg = 0;
        batteryarray[batcount] = 100*(b - .5*3770)/(.13*3770);    //vdd = 3770 for correct case
        batcount++;
        if(batcount == 50) {
            batcount = 0;
            for(int i=0;i<50;i++) {
                avg+=batteryarray[i];
            }
            avg=avg/50;
            if(avg<0)
                avg=0;
            else if(avg>100)
                avg=100;
            TextView battery = (TextView) findViewById(R.id.mybattery);
            String percent = Double.toString(avg);
            int period = percent.indexOf(".");
            percent = percent.substring(0,period);
            percent+= "%";
            battery.setText(percent);
        }
    }


    void startShowGraph() {
        isGraphInProgress = true;
        mRepeatTask.run();
    }

    void stopShowGraph() {
        setDefaultUI();
        isGraphInProgress = false;
        mHandler.removeCallbacks(mRepeatTask);
    }

    private Runnable mRepeatTask = new Runnable() {
        @Override
        public void run() {
            //updateGraph();

            if(mCounter > GRAPH_WINDOW){
                mLineGraph.LineGraphViewUp((mCounter - GRAPH_WINDOW + 24 ), mCounter);
                //	mLineGraph2.LineGraphViewUp((mCounter - GRAPH_WINDOW + 20), mCounter);
            }
            mGraphView.repaint();
            //mGraphView2.repaint();


            mHandler.postDelayed(mRepeatTask, (long) REFRESH_INTERVAL);
        }
    };

    @Override
    protected BleManager<WalkthroughMaster_ManagerCallback> initializeManager() {
        manager = WalkthroughMaster_Manager.getInstance(getApplicationContext());
        manager.setGattCallbacks(this);
        return manager;
    }


    @Override
    public void onServicesDiscovered(final boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    @Override
    public void onDeviceReady() {
        //startShowGraph(); for now
    }

    @Override
    public void onDeviceConnected() {
        super.onDeviceConnected();
        datalog.MakeNewLog();
    }

    @Override
    public void onHRSensorPositionFound(final String position) {
        //setHRSPositionOnVi/ew(position);
    }

    @Override
    public void onHRValueReceived(int[] x,int[] y, int[] z, int[] a,int[] b) {
        //updateGraph(x,y,z,a);
        //updateBattery(b[0]);
        datalog.appendLog(x,y,z,a,b);
    }

    @Override
    public void onDeviceDisconnected() {
        super.onDeviceDisconnected();
        //Reset the battery level to n/a when device is not connected
        /*TextView battery = (TextView) findViewById(R.id.mybattery);
        battery.setText("n/a");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NewLog = true;
                stopShowGraph();
                led.setChecked(false);
                setDefaultUI();
            }
        });*/
    }

    @Override
    protected void setDefaultUI() {
        //clearGraph();
    }

    private void clearGraph() {
        mLineGraph.clearGraph();
        mGraphView.repaint();
        mCounter = 0;
        mLineGraph.LineGraphViewUp((0), GRAPH_WINDOW);
        mGraphView.repaint();
    }

    public void setupViewPager() {
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(InstructionFragment.newInstance(0,"page #1"));
        pagerAdapter.addFragment(TimerFragment.newInstance(1,"Page #2"));
        pagerAdapter.addFragment(ContactTestFragment.newInstance("2","page #3"));
        vpPager.setAdapter(pagerAdapter);
    }

    //gets called when the next button is pressed
    public void setVpPager(int i) {
        vpPager.setCurrentItem(i);
    }

    public void startTheTimer() {
        TimerFragment timerFragment = (TimerFragment)pagerAdapter.getItem(1);
        timerFragment.myTimer.start();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //Leave Empty
    }

    public static class MyPagerAdapter extends FragmentStatePagerAdapter {
        private static int NUM_ITEMS = 3;
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

    }


}
