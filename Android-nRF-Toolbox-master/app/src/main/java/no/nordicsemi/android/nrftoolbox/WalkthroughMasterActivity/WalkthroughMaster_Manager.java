package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity.WalkthroughMaster_ManagerCallback;
import no.nordicsemi.android.nrftoolbox.parser.BodySensorLocationParser;
import no.nordicsemi.android.nrftoolbox.parser.HeartRateMeasurementParser;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;

/**
 * NFC_BLE_HYBRID_Manager class performs BluetoothGatt operations for connection, service discovery, enabling notification and reading characteristics. All operations required to connect to device with BLE HR
 * Service and reading heart rate values are performed here. NFC_BLE_HYBRID_Activity implements NFC_BLE_HYBRID_ManagerCallback in order to receive callbacks of BluetoothGatt operations
 */
public class WalkthroughMaster_Manager extends BleManager<WalkthroughMaster_ManagerCallback> {
    public final static UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
    private static final UUID HR_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    //Josh: UUIDs for gpio
    public static final UUID RX_SERVICE_UUID = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
    public static final UUID RX_CHAR_UUID = UUID.fromString("00001525-1212-efde-1523-785feabcd123");

    private int[] x = new int[2];
    private int[] y = new int[2];
    private int[] z = new int[2];
    private int[] a = new int[2];
    private int[] b = new int[2];
    /*
        public final static UUID UART_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
        private static final UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("00002A40-0000-1000-8000-00805f9b34fb");
        private static final UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    */
    private BluetoothGattCharacteristic mHRCharacteristic, mHRLocationCharacteristic, mRXCharacteristic; // Josh; mRXCharacteristic for gpio control

    private static WalkthroughMaster_Manager managerInstance = null;

    /**
     * singleton implementation of WalkthoughMaster_Manager class
     */
    public static synchronized WalkthroughMaster_Manager getInstance(final Context context) {
        if (managerInstance == null) {
            managerInstance = new WalkthroughMaster_Manager(context);
        }
        return managerInstance;
    }

    public WalkthroughMaster_Manager(final Context context) {
        super(context);
    }

    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

        @Override
        protected Queue<Request> initGatt(final BluetoothGatt gatt) {
            final LinkedList<Request> requests = new LinkedList<>();
            if (mHRLocationCharacteristic != null)
                requests.push(Request.newReadRequest(mHRLocationCharacteristic));
            requests.push(Request.newEnableNotificationsRequest(mHRCharacteristic));
            return requests;
        }

        @Override
        protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
            final BluetoothGattService RXService = gatt.getService(RX_SERVICE_UUID);
            if (service != null) {
                mHRCharacteristic = service.getCharacteristic(HR_CHARACTERISTIC_UUID);
                mRXCharacteristic = RXService.getCharacteristic(RX_CHAR_UUID);
            }
            return mHRCharacteristic != null && mRXCharacteristic != null;
        }

        @Override
        protected boolean isOptionalServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
            if (service != null) {
                mHRLocationCharacteristic = service.getCharacteristic(HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
            }
            return mHRLocationCharacteristic != null;
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (mLogSession != null)
                Logger.a(mLogSession, BodySensorLocationParser.parse(characteristic));

            final String sensorPosition = getBodySensorPosition(characteristic.getValue()[0]);
            //This will send callback to NFC_BLE_HYBRID_Activity when HR sensor position on body is found in HR device
            mCallbacks.onHRSensorPositionFound(sensorPosition);
        }

        @Override
        protected void onDeviceDisconnected() {
            mHRLocationCharacteristic = null;
            mHRCharacteristic = null;
            mRXCharacteristic = null;
        }

        @Override
        public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (mLogSession != null)
                Logger.a(mLogSession, HeartRateMeasurementParser.parse(characteristic));


            for(int i = 0; i<2; i++){
                x[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i*10);
                y[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i*10+2);
                z[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i*10+4);
                a[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i*10+6);
                b[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,i*10+8);
                //	vdd[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i*12+10);


                //scg[i] =characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UIT16_INVERT, i+10);
            }

            mCallbacks.onHRValueReceived(x,y,z,a,b);

        }

        //Josh, currently doing nothing
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };


    /** Josh
     *  This method will handle sending data to server
     */
    public void writeRXCharacteristic(byte[] value)
    {
        if (mRXCharacteristic != null){
            mRXCharacteristic.setValue(value);
            writeCharacteristic(mRXCharacteristic);
        }
        else{
            Log.v("log", "mRXCharacteristic not found");
        }
    }

    /**
     * This method will decode and return Heart rate sensor position on body
     */
    private String getBodySensorPosition(final byte bodySensorPositionValue) {
        final String[] locations = getContext().getResources().getStringArray(R.array.hrs_locations);
        if (bodySensorPositionValue > locations.length)
            return getContext().getString(R.string.hrs_location_other);
        return locations[bodySensorPositionValue];
    }

    /**
     * This method will check if Heart rate value is in 8 bits or 16 bits
     */

}