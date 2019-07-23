/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.GraphicalView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.MainActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;

/**
 * NFC_BLE_HYBRID_Activity is the main Heart rate activity. It implements NFC_BLE_HYBRID_ManagerCallback to receive callbacks from NFC_BLE_HYBRID_Manager class. The activity supports portrait and landscape orientations. The activity
 * uses external library AChartEngine to show real time graph of HR values.
 */
// TODO The NFC_BLE_HYBRID_Activity should be rewritten to use the service approach, like other do.
public class NFC_BLE_HYBRID_Activity extends BleProfileActivity implements NFC_BLE_HYBRID_ManagerCallback {
	@SuppressWarnings("unused")
	private final String TAG = "NFC_BLE_HYBRID_Activity";

	private final static String GRAPH_STATUS = "graph_status";
	private final static String GRAPH_COUNTER = "graph_counter";
	public final static int GRAPH_WINDOW = 2000;  //181015, org=10

	List<String> UID_List = new ArrayList<>();
	private String[] UID_Array = new String[4];
	private double[] batteryarray = new double[50];
	private int batcount = 0;

	private log datalog = new log();

	private Switch log;
	private Switch led;
	boolean NewLog = true;

	private NFC_BLE_HYBRID_Manager manager;

	private final static int REFRESH_INTERVAL = 100;//1 second interval
	//	private short axis = 0;


	private Handler mHandler = new Handler();

	private boolean isGraphInProgress = false;

	private GraphicalView mGraphView,mGraphView2;
	private LineGraphView mLineGraph = new LineGraphView();
//	private LineGraphView mLineGraph2 = new LineGraphView();


	private int mCounter = 0;

	private log logger = new log();

	private int[] ecg_hrs = new int[10];
	private int[] scg_hrs = new int[10];

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hrs);
		setGUI();
		// Listener for Led/GPIO switch
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

		//	initialize_session();

	}


	protected void onResume() {
		super.onResume();
	}


	private void setGUI() {

		log = (Switch) findViewById(R.id.switch1);
		led = (Switch) findViewById(R.id.led_switch);
		mLineGraph = mLineGraph.getLineGraphView();
		//	mLineGraph2 = mLineGraph2.getLineGraphView();
		showGraph();
	}

	private void showGraph() {
		mGraphView = mLineGraph.getView(this);
		ViewGroup layout = (ViewGroup) findViewById(R.id.graph_temp1);
		layout.addView(mGraphView);
//		mGraphView2 = mLineGraph2.getView(this);
//		ViewGroup layout2 = (ViewGroup) findViewById(R.id.graph_temp2);
//		layout2.addView(mGraphView2);


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
		return NFC_BLE_HYBRID_Manager.HR_SERVICE_UUID;
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
		batteryarray[batcount] = b*.1948-367.89;
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
	protected BleManager<NFC_BLE_HYBRID_ManagerCallback> initializeManager() {
		manager = NFC_BLE_HYBRID_Manager.getInstance(getApplicationContext());
		manager.setGattCallbacks(this);
		return manager;
	}

//	private void setHRSValueOnView(final int i, final String UID_String) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				final String uidtext = "UID: " + UID_String;
//				if (i == 0) {
//					UID1_Text.setText(uidtext);
//				} else if (i == 1) {
//					UID2_Text.setText(uidtext);
//
//				} else if (i == 2) {
//					UID3_Text.setText(uidtext);
//
//				} else if (i == 3) {
//					UID4_Text.setText(uidtext);
//				}
//
//			}
//		});
//	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onDeviceReady() {
		startShowGraph();
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
		updateGraph(x,y,z,a);

		datalog.appendLog(x,y,z,a,b);
		updateBattery(b[0]);
	}

	public void onClose(View view) {
	//	NFC_BLE_HYBRID_Activity.
	//	Intent intent = new Intent(NFC_BLE_HYBRID_Activity.this, MainActivity.class);
	//	startActivity(intent);
		finish();
	}

	@Override
	public void onDeviceDisconnected() {
		super.onDeviceDisconnected();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				NewLog = true;
				stopShowGraph();
				setDefaultUI();
			}
		});
	}

	@Override
	protected void setDefaultUI() {
		clearGraph();
	}

	private void clearGraph() {
		mLineGraph.clearGraph();
		mGraphView.repaint();
		mCounter = 0;
		mLineGraph.LineGraphViewUp((0), GRAPH_WINDOW);
		mGraphView.repaint();
//		mLineGraph2.LineGraphViewUp((0), GRAPH_WINDOW);
//		mGraphView2.repaint();


	}

}

