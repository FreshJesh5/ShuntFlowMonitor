package no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface WalkthroughMaster_ManagerCallback extends BleManagerCallbacks {

    /**
     * Called when the sensor position information has been obtained from the sensor
     *
     * @param position the sensor position
     */
    public void onHRSensorPositionFound(String position);

    /**
     * Called when new Heart Rate value has been obtained from the sensor
     *
     * @param axis_x the new value
     */
    //public void onHRValueReceived(int value);
    //public void onHRValueReceived(int value1, int value2, int value3, int value4, int value5, int value6,
    //							    int axis_x,int axis_y,int axis_z);
    public void onHRValueReceived(int[] x,int[] y,int[] z, int[] a, int[] b);
}
