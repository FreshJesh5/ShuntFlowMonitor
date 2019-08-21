package no.nordicsemi.android.nrftoolbox;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import no.nordicsemi.android.nrftoolbox.WalkthroughMasterActivity.WalkthroughMasterActivity;
import no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid.NFC_BLE_HYBRID_Activity;

public class MainActivity extends AppCompatActivity {
    public boolean mFlag1=false;
    public boolean mFlag2=false;
    public boolean mFlag3=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }
    public void beginHybridActivity(android.view.View view) {
        Intent intent = new Intent(MainActivity.this, NFC_BLE_HYBRID_Activity.class);
        startActivity(intent);
    }
    public void beginWalkthroughActivity(android.view.View view) {
        Intent intent = new Intent(MainActivity.this, WalkthroughMasterActivity.class);
        startActivity(intent);
    }
    public void beginUploadDataActivity(android.view.View view) {
        Intent intent = new Intent(MainActivity.this, UploadDataActivity.class);
        startActivity(intent);
    }
}
